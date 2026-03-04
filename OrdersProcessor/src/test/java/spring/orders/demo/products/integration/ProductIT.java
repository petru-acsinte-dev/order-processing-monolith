package spring.orders.demo.products.integration;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.shared.AbstractIntegrationTestBase;

@Transactional
class ProductIT extends AbstractIntegrationTestBase{

	private static final String FIELD_AMOUNT = "amount"; //$NON-NLS-1$
	private static final String FIELD_SKU = "sku"; //$NON-NLS-1$
	private static final String FIELD_NAME = "name"; //$NON-NLS-1$
	private static final Object FIELD_CURRENCY = "currency"; //$NON-NLS-1$
	private static final String FIELD_EXTERNAL_ID = "externalId"; //$NON-NLS-1$
	private static final String MEMBER_TEMPLATE = "$[%d].%s"; //$NON-NLS-1$
	private static final String MEMBER_COST_TEMPLATE = "$[%d].cost.%s"; //$NON-NLS-1$
	private static final Logger log = LoggerFactory.getLogger(ProductIT.class);
	private static final int EXPECTED_NO = 120; // expected predefined products
	private static final String ISO_4217_REGEX = "^[A-Z]{3}$"; //$NON-NLS-1$

	@BeforeEach
	void login() throws Exception {
		loginAs(Constants.ADMIN, Constants.ADMIN);
	}

	@Test
	@DisplayName("Tests retrieving existing products")
	void testGetAllProducts() throws Exception {
		final int randomSelection = new Random().nextInt(110);
		final MvcResult result = mockMvc
				.perform(get(Constants.PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$.length()").value(EXPECTED_NO)) //$NON-NLS-1$
				.andExpect(jsonPath(MEMBER_TEMPLATE, randomSelection, FIELD_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(MEMBER_TEMPLATE, randomSelection, FIELD_EXTERNAL_ID).value(matchesPattern(UUID_REGEX)))
				.andExpect(jsonPath(MEMBER_TEMPLATE, randomSelection, FIELD_NAME).isNotEmpty())
				.andExpect(jsonPath(MEMBER_TEMPLATE, randomSelection, FIELD_SKU).isNotEmpty())
				.andExpect(jsonPath(MEMBER_COST_TEMPLATE, randomSelection, FIELD_AMOUNT).isNumber())
				.andExpect(jsonPath(MEMBER_COST_TEMPLATE, randomSelection, FIELD_AMOUNT).value(Matchers.greaterThanOrEqualTo(0.0D)))
				.andExpect(jsonPath(MEMBER_COST_TEMPLATE, randomSelection, FIELD_CURRENCY).isNotEmpty())
				.andExpect(jsonPath(MEMBER_COST_TEMPLATE, randomSelection, FIELD_CURRENCY).value(matchesPattern(ISO_4217_REGEX)))
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}
	}

	@Override
	protected Logger getLog() {
		return log;
	}

}
