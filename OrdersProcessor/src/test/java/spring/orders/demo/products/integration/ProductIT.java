package spring.orders.demo.products.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import spring.orders.demo.AbstractIntegrationTestBase;
import spring.orders.demo.constants.Constants;

class ProductIT extends AbstractIntegrationTestBase{

	private static final Logger log = LoggerFactory.getLogger(ProductIT.class);
	private static final int EXPECTED_NO = 120; // expected predefined products

	@BeforeEach
	void login() throws Exception {
		loginAs(Constants.ADMIN, Constants.ADMIN);
	}

	@Test
	@DisplayName("Tests retrieving existing products")
	void testGetAllProducts() throws Exception {
		final MvcResult result = mockMvc
				.perform(get(Constants.PRODUCTS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$.length()").value(EXPECTED_NO)) //$NON-NLS-1$
//				.andExpect(jsonPath("$[0].username").value(Constants.ADMIN)) //$NON-NLS-1$
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
