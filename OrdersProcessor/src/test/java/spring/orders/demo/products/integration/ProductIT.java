package spring.orders.demo.products.integration;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Currency;
import java.util.Random;

import org.hamcrest.Matchers;
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

import spring.orders.demo.constants.Constants;
import spring.orders.demo.products.dto.CreateProductRequest;
import spring.orders.demo.products.dto.MoneyDTO;
import spring.orders.demo.products.dto.ProductResponse;
import spring.orders.demo.shared.AbstractIntegrationTestBase;

@Transactional
class ProductIT extends AbstractIntegrationTestBase{

	private static final String USD = "USD"; //$NON-NLS-1$
	private static final String TEST_ADDRESS = "nowhere"; //$NON-NLS-1$
	private static final String USER_EMAIL = "user@dev.com"; //$NON-NLS-1$
	private static final String TEST_PSWD = "User1234"; //$NON-NLS-1$
	private static final String TEST_USER = "user1234"; //$NON-NLS-1$
	private static final String FIELD_AMOUNT = "amount"; //$NON-NLS-1$
	private static final String FIELD_SKU = "sku"; //$NON-NLS-1$
	private static final String FIELD_NAME = "name"; //$NON-NLS-1$
	private static final Object FIELD_CURRENCY = "currency"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_ID = "externalId"; //$NON-NLS-1$
	private static final String ARRAY_MEMBR_TMPLT = "$[%d].%s"; //$NON-NLS-1$
	private static final String ARRAY_MEMBR_COST_TMPLT = "$[%d].cost.%s"; //$NON-NLS-1$
	private static final String MEMBR_COST_TMPLT = "$.cost.%s"; //$NON-NLS-1$
	private static final String MEMBR_TMPLT = "$.%s"; //$NON-NLS-1$
	private static final Logger log = LoggerFactory.getLogger(ProductIT.class);
	private static final int EXPECTED_NO = 120; // expected predefined products
	private static final String ISO_4217_REGEX = "^[A-Z]{3}$"; //$NON-NLS-1$

	@Override
	protected Logger getLog() {
		return log;
	}

	@BeforeEach
	void login() throws Exception {
		loginAs(Constants.ADMIN, Constants.ADMIN);
	}

	@Test
	@DisplayName("Tests retrieving existing products")
	void testGetAllProducts() throws Exception {
		doGetAllProducts();
	}

	@Test
	@DisplayName("Tests retrieving existing products as a regular user")
	@Rollback
	void testGetAllProductsAsRegularUser() throws Exception {
		createAndValidateUser(TEST_USER, USER_EMAIL, TEST_PSWD, TEST_ADDRESS);

		loginAs(TEST_USER, TEST_PSWD);

		doGetAllProducts();
	}

	@Test
	@DisplayName("Tests creating a product")
	void testCreateProduct() throws Exception {
		doCreateProduct(true);
	}

	@Test
	@DisplayName("Negative: Tests creating a product as a regular user")
	@Rollback
	void testCreateProductAsRegularUser() throws Exception {
		createAndValidateUser(TEST_USER, USER_EMAIL, TEST_PSWD, TEST_ADDRESS);

		loginAs(TEST_USER, TEST_PSWD);

		doCreateProduct(false);
	}

	private ProductResponse doCreateProduct(boolean expectSuccessful) throws Exception {
		final var createRequest = new CreateProductRequest();
		final String expectedSku = "SKU-0000246"; //$NON-NLS-1$
		createRequest.setSku(expectedSku);
		final String expectedName = "Test product"; //$NON-NLS-1$
		createRequest.setName(expectedName);
		final String expectedDesc = "Dummy product"; //$NON-NLS-1$
		createRequest.setDescription(expectedDesc);
		final MoneyDTO moneyDTO = new MoneyDTO(BigDecimal.valueOf(123.45D), Currency.getInstance(USD));
		createRequest.setCost(moneyDTO);
		final ResultActions resultActions = mockMvc
				.perform(post(Constants.PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.characterEncoding(StandardCharsets.UTF_8)
						.content(objectMapper.writeValueAsString(createRequest)));
		if (expectSuccessful) {
			resultActions
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_EXTERNAL_ID).value(matchesPattern(UUID_REGEX)))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_NAME).value(expectedName))
				.andExpect(jsonPath(MEMBR_TMPLT, FIELD_SKU).value(expectedSku))
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_AMOUNT).value(123.45D))
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(MEMBR_COST_TMPLT, FIELD_CURRENCY).value(USD));
			final MvcResult result = resultActions.andReturn();
			final String content = result.getResponse().getContentAsString();
			return objectMapper.readValue(content, ProductResponse.class);

		}
		resultActions
				.andExpect(status().isForbidden());
		return null;
	}

	private void doGetAllProducts() throws Exception {
		final int randomSelection = new Random().nextInt(110);
		final MvcResult result = mockMvc
				.perform(get(Constants.PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$.length()").value(EXPECTED_NO)) //$NON-NLS-1$
				.andExpect(jsonPath(ARRAY_MEMBR_TMPLT, randomSelection, FIELD_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(ARRAY_MEMBR_TMPLT, randomSelection, FIELD_EXTERNAL_ID).value(matchesPattern(UUID_REGEX)))
				.andExpect(jsonPath(ARRAY_MEMBR_TMPLT, randomSelection, FIELD_NAME).isNotEmpty())
				.andExpect(jsonPath(ARRAY_MEMBR_TMPLT, randomSelection, FIELD_SKU).isNotEmpty())
				.andExpect(jsonPath(ARRAY_MEMBR_COST_TMPLT, randomSelection, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(ARRAY_MEMBR_COST_TMPLT, randomSelection, FIELD_AMOUNT).value(Matchers.greaterThanOrEqualTo(0.0D)))
				.andExpect(jsonPath(ARRAY_MEMBR_COST_TMPLT, randomSelection, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(ARRAY_MEMBR_COST_TMPLT, randomSelection, FIELD_CURRENCY).value(matchesPattern(ISO_4217_REGEX)))
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}
	}

}
