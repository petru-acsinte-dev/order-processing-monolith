package spring.orders.demo.orders.integration;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.OrderStatus;
import spring.orders.demo.orders.dto.CreateOrderRequest;
import spring.orders.demo.orders.dto.OrderLineRequest;
import spring.orders.demo.orders.dto.ProductResponse;
import spring.orders.demo.shared.AbstractIntegrationTestBase;
import spring.orders.demo.users.dto.CustomerUserResponse;

@Transactional
class OrderIT extends AbstractIntegrationTestBase {

	private final Logger log = LoggerFactory.getLogger(OrderIT.class);

	private static final String MEMBR_TMPLT = "$.%s"; //$NON-NLS-1$
	private static final String MEMBR_TOTAL_TMPLT = "$.orderTotal.%s"; //$NON-NLS-1$
	private static final String CNT_ARRAY_MEMBR_TMPLT = "$.%s[%d].%s"; //$NON-NLS-1$
	private static final String CNT_ARRAY_MEMBR_COST_TMPLT = "$.%s[%d].cost.%s"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_ID = "externalId"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_USER_ID = "customerExternalId"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_PRODUCT_ID = "productExternalId"; //$NON-NLS-1$
	private static final String FIELD_STATUS = "status"; //$NON-NLS-1$
	private static final String FIELD_AMOUNT = "amount"; //$NON-NLS-1$
	private static final String FIELD_CURRENCY = "currency"; //$NON-NLS-1$
	private static final String FIELD_ORDER_LINES = "orderLines"; //$NON-NLS-1$
	private static final String FIELD_PRODUCT_NAME = "productName"; //$NON-NLS-1$
	private static final String FIELD_QUANTITY = "quantity"; //$NON-NLS-1$
	private static final String FIELD_LINE_TOTAL = "lineTotal"; //$NON-NLS-1$

	private String testUserId;

	@BeforeEach
	void setup() throws Exception {
		loginAs(Constants.ADMIN, Constants.ADMIN);

		final CustomerUserResponse regularUser = createAndValidateUser(TEST_USER, USER_EMAIL, TEST_PSWD, TEST_ADDRESS);
		this.testUserId = regularUser.getExternalId();

		loginAs(TEST_USER, TEST_PSWD);
	}

	@Override
	protected Logger getLog() {
		return log;
	}

	@Test
	@DisplayName("Tests creating an order with a single product")
	@Rollback
	void testCreateOrder() throws Exception {
		// selecting a random product
		final List<ProductResponse> products = getProducts();
		final Random random = new Random();
		final ProductResponse product = products.get(random.nextInt(ProductIT.PAGE_SIZE));

		final CreateOrderRequest createRequest = new CreateOrderRequest();
		final OrderLineRequest lineRequest = new OrderLineRequest();
		lineRequest.setProductId(UUID.fromString(product.getExternalId()));
		final int quantity = 2;
		lineRequest.setQuantity(quantity);
		createRequest.setProducts(List.of(lineRequest));

		final ResultActions resultActions = mockMvc.perform(post(Constants.ORDERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(createRequest)));

		resultActions
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).isNotEmpty()) // externalId
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID)
					.value(matchesPattern(UUID_REGEX)))
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID)
					.value(matchesPattern(UUID_REGEX))) // customerExternalId
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_USER_ID)
					.value(testUserId))
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_STATUS)
					.value(OrderStatus.CREATED)) // status
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT).isNumber())
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_AMOUNT)
					.value(product.getCost().getAmount().multiply(BigDecimal.valueOf(quantity)))) // orderTotal.amount
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY).isNotEmpty())
			.andExpect(jsonPath(MEMBR_TOTAL_TMPLT, FIELD_CURRENCY)
					.value(product.getCost().getCurrency().getCurrencyCode())); // orderTotal.currency

		// OrderLineDTO
		resultActions
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).isArray())
			.andExpect(jsonPath(MEMBR_TMPLT, FIELD_ORDER_LINES).isNotEmpty())
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_EXTERNAL_PRODUCT_ID)
					.value(product.getExternalId())) // productExternalId
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_PRODUCT_NAME)
					.value(product.getName())) // productName
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_QUANTITY)
					.value(quantity)) // quantity
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_TMPLT, FIELD_ORDER_LINES, 0, FIELD_LINE_TOTAL)
					.value(product.getCost().getAmount().multiply(BigDecimal.valueOf(quantity)))) // lineTotal
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_AMOUNT)
					.value(product.getCost().getAmount()))  //cost.amount
			.andExpect(jsonPath(CNT_ARRAY_MEMBR_COST_TMPLT, FIELD_ORDER_LINES, 0, FIELD_CURRENCY)
					.value(product.getCost().getCurrency().getCurrencyCode())); // cost.currency
	}

	@Test
	@DisplayName("Tests creating an order with a non-existent product")
	@Rollback
	void testCreateOrderForMissingProduct() throws Exception {
		// selecting a random product
		final CreateOrderRequest createRequest = new CreateOrderRequest();
		final OrderLineRequest lineRequest = new OrderLineRequest();
		lineRequest.setProductId(UUID.randomUUID());
		final int quantity = 2;
		lineRequest.setQuantity(quantity);
		createRequest.setProducts(List.of(lineRequest));

		final ResultActions resultActions = mockMvc.perform(post(Constants.ORDERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(createRequest)));

		resultActions
			.andExpect(status().isNotFound());
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
