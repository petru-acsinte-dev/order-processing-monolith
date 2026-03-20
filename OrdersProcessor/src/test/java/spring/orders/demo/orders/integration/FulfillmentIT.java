package spring.orders.demo.orders.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.ship.Status;
import spring.orders.demo.orders.dto.CreateOrderRequest;
import spring.orders.demo.orders.dto.OrderLineRequest;
import spring.orders.demo.orders.dto.OrderResponse;
import spring.orders.demo.orders.dto.ProductResponse;
import spring.orders.demo.shared.AbstractIntegrationTestBase;
import spring.orders.demo.ship.dto.FulfillmentResponse;

@Transactional
class FulfillmentIT extends AbstractIntegrationTestBase {

	private static Logger log = LoggerFactory.getLogger(FulfillmentIT.class);

	private static final String MEMBR_TMPLT = "$.%s"; //$NON-NLS-1$
	private static final String FIELD_STATUS = "status"; //$NON-NLS-1$

	private static final String ORDER_STATUS_CONFIRMED = "CONFIRMED"; //$NON-NLS-1$
	private static final String ORDER_STATUS_SHIPPED = "SHIPPED"; //$NON-NLS-1$
	private static final String ORDER_STATUS_CREATED = "CREATED"; //$NON-NLS-1$

	// used by tests to remember order outside the transactional context (workaround for events)
	private UUID orderId = null;
	private Status expectedStatus = null;

	@Override
	protected Logger getLog() {
		return log;
	}

	@BeforeEach
	void setup() throws Exception {
		loginAs(Constants.ADMIN, Constants.ADMIN);

		createAndValidateUser(TEST_USER, USER_EMAIL, TEST_PSWD, TEST_ADDRESS);

		loginAs(TEST_USER, TEST_PSWD);
	}

	@Test
	@DisplayName("Confirms an order and retrieves its fulfillment")
	@Rollback
	void testConfirmOrderCheckFulfillment() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		final ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		final int quantity = 2;
		final ResultActions creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions.andExpect(status().isCreated())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CREATED));

		final MvcResult created = creationActions.andReturn();
		final String orderLocation = created.getResponse().getHeader(HttpHeaders.LOCATION);
		final OrderResponse newOrder = objectMapper.readValue(
											created.getResponse().getContentAsString(),
											OrderResponse.class);
		orderId = newOrder.getExternalId();

		confirmOrder(orderLocation);

		expectedStatus = Status.READY_TO_SHIP;
	}

	@Test
	@DisplayName("Confirms an order and ships its fulfillment")
	@Rollback
	void testConfirmOrderShipFulfillment() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		final ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		final int quantity = 2;
		final ResultActions creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions.andExpect(status().isCreated())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CREATED));

		final MvcResult created = creationActions.andReturn();
		final String orderLocation = created.getResponse().getHeader(HttpHeaders.LOCATION);
		final OrderResponse newOrder = objectMapper.readValue(
											created.getResponse().getContentAsString(),
											OrderResponse.class);
		orderId = newOrder.getExternalId();

		confirmOrder(orderLocation);

		loginAs(Constants.ADMIN, Constants.ADMIN);

		shipOrder(Constants.FULFILLMENTS_PATH + '/' + orderId);

		expectedStatus = Status.SHIPPED;

	}

	@Test
	@DisplayName("Retrieves all available fulfillment")
	@Rollback
	void testGetAllFulfillments() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		final int quantity = 2;
		ResultActions creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions.andExpect(status().isCreated())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CREATED));

		MvcResult created = creationActions.andReturn();
		final String order1Location = created.getResponse().getHeader(HttpHeaders.LOCATION);
		OrderResponse newOrder = objectMapper.readValue(created.getResponse().getContentAsString(),
				OrderResponse.class);
		final UUID order1Id = newOrder.getExternalId();

		confirmOrder(order1Location);

		product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		creationActions = doCreateOrder(List.of(Pair.of(product, quantity)));
		creationActions.andExpect(status().isCreated())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CREATED));

		created = creationActions.andReturn();
		final String order2Location = created.getResponse().getHeader(HttpHeaders.LOCATION);
		newOrder = objectMapper.readValue(created.getResponse().getContentAsString(), OrderResponse.class);
		final UUID order2Id = newOrder.getExternalId();

		confirmOrder(order2Location);

		checkFulfillments(Status.READY_TO_SHIP, List.of(order1Id, order2Id));
	}

	// this needs to happen after the test or else events don't take effect
	@AfterEach
	void postTestCheck() throws Exception {
		if (null != orderId) {
			loginAs(Constants.ADMIN, Constants.ADMIN);
			// might need to wait for eventual consistency
			Awaitility.await(expectedStatus.name())
				.pollInSameThread()
				.pollDelay(1, TimeUnit.SECONDS)
				.atMost(3, TimeUnit.SECONDS)
				.untilAsserted(() ->
					assertEquals(HttpStatus.OK.value(), checkFulfillment(orderId, expectedStatus.name()))
			);
		}
	}

	private void checkFulfillments(Status expectedStatus, List<UUID> orderExternalIds) throws Exception {

		loginAs(Constants.ADMIN, Constants.ADMIN);

		final MvcResult actions = mockMvc.perform(get(Constants.FULFILLMENTS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR).isArray()) //$NON-NLS-1$
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR + ".length()").value(2)) //$NON-NLS-1$ //$NON-NLS-2$
			.andReturn();

		final JsonNode root = objectMapper.readTree(actions.getResponse().getContentAsString());
		final JsonNode contentNode = root.get(Constants.PAGE_CONTENT_ATTR);
		final List<FulfillmentResponse> contents =
				objectMapper.convertValue(contentNode, new TypeReference<List<FulfillmentResponse>>() {});

		for (final FulfillmentResponse fulfillment : contents) {
			assertEquals(expectedStatus.name(), fulfillment.getStatus());
			assertTrue(orderExternalIds.contains(fulfillment.getOrderExternalId()));
		}
	}

	private int checkFulfillment(UUID orderExternalId, String expectedStatus) throws Exception {
		return mockMvc.perform(get(Constants.FULFILLMENTS_PATH + '/' + orderExternalId)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(expectedStatus))
			.andReturn()
			.getResponse()
			.getStatus();
	}

	private ResultActions updateStatus(String endPoint, String pathVariable) throws Exception {
		return mockMvc.perform(post(endPoint + pathVariable)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()));
	}

	private void confirmOrder(String orderLocation) throws Exception {
		updateStatus(orderLocation, "/confirm") //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CONFIRMED));
	}

	private void shipOrder(String fulfillmentLocation) throws Exception {
		updateStatus(fulfillmentLocation, "/ship") //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_SHIPPED));
	}

	private ResultActions doCreateOrder(List<Pair<ProductResponse, Integer>> lines) throws Exception {
		final CreateOrderRequest createRequest = new CreateOrderRequest();
		for (final Pair<ProductResponse, Integer> line : lines) {
			final OrderLineRequest lineRequest = new OrderLineRequest();
			lineRequest.setProductId(UUID.fromString(line.getFirst().getExternalId()));
			lineRequest.setQuantity(line.getSecond());
			createRequest.getProducts().add(lineRequest);
		}

		return mockMvc.perform(post(Constants.ORDERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(createRequest)));
	}

	// helper method that retrieves the first products page
	private List<ProductResponse> getProducts() throws Exception {
		final MvcResult result = mockMvc.perform(get(Constants.PRODUCTS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.param("size", String.valueOf(ProductIT.PAGE_SIZE))) //$NON-NLS-1$
			.andReturn();
		final String content = result.getResponse().getContentAsString();
		final JsonNode root = objectMapper.readTree(content);
		final JsonNode contentNode = root.get(Constants.PAGE_CONTENT_ATTR);

		return objectMapper.readValue(
		        contentNode.toString(),
		        new TypeReference<List<ProductResponse>>() {}
		);
	}

}
