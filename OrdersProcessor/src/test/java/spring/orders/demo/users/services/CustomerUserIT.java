package spring.orders.demo.users.services;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.orders.demo.Constants;

@AutoConfigureMockMvc
class CustomerUserIT extends AbstractIntegrationTestBase {

	private final Logger log = org.slf4j.LoggerFactory.getLogger(CustomerUserIT.class);

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void testGetAllUsers() throws Exception {
		final MvcResult result = mockMvc
				.perform(get(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(Constants.X_USER, Constants.USER_ADMIN))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$.length()").value(1)) //$NON-NLS-1$
				.andExpect(jsonPath("$[0].username").value(Constants.USER_ADMIN)) //$NON-NLS-1$
				.andReturn();
		log.debug(result.getResponse().getContentAsString());
	}
}
