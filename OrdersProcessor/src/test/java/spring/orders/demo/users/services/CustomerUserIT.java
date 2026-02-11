package spring.orders.demo.users.services;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.orders.demo.Constants;
import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.entities.UserRole;
import spring.orders.demo.users.entities.UserStatus;

@AutoConfigureMockMvc
class CustomerUserIT extends AbstractIntegrationTestBase {

	private final Logger log = org.slf4j.LoggerFactory.getLogger(CustomerUserIT.class);

	private final String firstUsername = "bobby"; //$NON-NLS-1$
	private final String firstEmail = "bobby@order.processor.com"; //$NON-NLS-1$
	private final String firstAddressLine1 = "NY NY"; //$NON-NLS-1$

	private final String secondUsername = "dan"; //$NON-NLS-1$
	private final String secondEmail = "dan@order.processor.com"; //$NON-NLS-1$
	private final String secondAddressLine1 = "LA LA"; //$NON-NLS-1$

	private static final String JSON_PATH_EXTERNAL_ID = "$.externalId"; //$NON-NLS-1$
	private static final String JSON_PATH_ROLE = "$.role"; //$NON-NLS-1$
	private static final String JSON_PATH_STATUS = "$.status"; //$NON-NLS-1$
	private static final String JSON_PATH_ADDRESS_LINE1 = "$.address.addressLine1"; //$NON-NLS-1$
	private static final String JSON_PATH_EMAIL = "$.email"; //$NON-NLS-1$
	private static final String JSON_PATH_USERNAME = "$.username"; //$NON-NLS-1$

	private static final String UUID_REGEX = "[a-fA-F0-9]{8}-([a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}"; //$NON-NLS-1$

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
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$.length()").value(1)) //$NON-NLS-1$
				.andExpect(jsonPath("$[0].username").value(Constants.USER_ADMIN)) //$NON-NLS-1$
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}
	}

	@Test
	void createUsers() throws Exception {
		final var createRequest = new CreateCustomerUserRequest();
		createRequest.setUsername(firstUsername);
		createRequest.setEmail(firstEmail);
		createRequest.setAddress(new AddressDTO(firstAddressLine1));

		// first user
		mockMvc.perform(post(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(Constants.X_USER, Constants.USER_ADMIN)
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding(StandardCharsets.UTF_8)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(JSON_PATH_USERNAME).value(firstUsername))
				.andExpect(jsonPath(JSON_PATH_EMAIL).value(firstEmail))
				.andExpect(jsonPath(JSON_PATH_ADDRESS_LINE1).value(firstAddressLine1))
				.andExpect(jsonPath(JSON_PATH_STATUS).value(UserStatus.ACTIVE))
				.andExpect(jsonPath(JSON_PATH_ROLE).value(UserRole.USER))
				.andExpect(jsonPath(JSON_PATH_EXTERNAL_ID).isNotEmpty());

		final var createRequest2 = new CreateCustomerUserRequest();
		createRequest2.setUsername(secondUsername);
		createRequest2.setEmail(secondEmail);
		createRequest2.setAddress(new AddressDTO(secondAddressLine1));

		// second user
		mockMvc.perform(post(Constants.USERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.X_USER, Constants.USER_ADMIN)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(createRequest2)))
		.andExpect(status().isCreated())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(jsonPath(JSON_PATH_USERNAME).value(secondUsername))
		.andExpect(jsonPath(JSON_PATH_EMAIL).value(secondEmail))
		.andExpect(jsonPath(JSON_PATH_ADDRESS_LINE1).value(secondAddressLine1))
		.andExpect(jsonPath(JSON_PATH_STATUS).value(UserStatus.ACTIVE))
		.andExpect(jsonPath(JSON_PATH_ROLE).value(UserRole.USER))
		.andExpect(jsonPath(JSON_PATH_EXTERNAL_ID).isNotEmpty())
		.andExpect(jsonPath(JSON_PATH_EXTERNAL_ID, matchesPattern(UUID_REGEX)));

		// getting both users (+ ADMIN)
		mockMvc.perform(get(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(Constants.X_USER, Constants.USER_ADMIN))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$.length()").value(3)); //$NON-NLS-1$
	}
}
