package spring.orders.demo.orders.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
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

@Transactional
class FulfillmentIT extends AbstractIntegrationTestBase {

	private static Logger log = LoggerFactory.getLogger(FulfillmentIT.class);

	private static final String MEMBR_TMPLT = "$.%s"; //$NON-NLS-1$
	private static final String FIELD_STATUS = "status"; //$NON-NLS-1$

	private static final String ORDER_STATUS_CONFIRMED = "CONFIRMED"; //$NON-NLS-1$
	private static final String ORDER_STATUS_CANCELLED = "CANCELLED"; //$NON-NLS-1$
	private static final String ORDER_STATUS_CREATED = "CREATED"; //$NON-NLS-1$

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

	private UUID orderId = null;
	private Status expectedStatus = null;
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

		confirm(orderLocation);

		expectedStatus = Status.READY_TO_SHIP;
	}

	// this needs to happen after the test or else events don't take effect
	@AfterEach
	void check() throws Exception {
		if (null != orderId) {
			loginAs(Constants.ADMIN, Constants.ADMIN);

			checkFulfillment(orderId, expectedStatus.name());
		}
	}

	private void checkFulfillment(UUID orderExternalId, String expectedStatus) throws Exception {
		mockMvc.perform(get(Constants.FULFILLMENTS_PATH + '/' + orderExternalId)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(expectedStatus));
	}

	private ResultActions updateStatus(String orderLocation, String endpoint) throws Exception {
		return mockMvc.perform(post(orderLocation + endpoint)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer()));
	}

	private void confirm(String orderLocation) throws Exception {
		updateStatus(orderLocation, "/confirm") //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CONFIRMED));
	}

	private void cancel(String orderLocation) throws Exception {
		updateStatus(orderLocation, "/cancel") //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS).value(ORDER_STATUS_CANCELLED));
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
