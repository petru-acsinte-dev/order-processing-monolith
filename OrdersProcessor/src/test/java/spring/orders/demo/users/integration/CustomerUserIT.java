package spring.orders.demo.users.integration;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import spring.orders.demo.Constants;
import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.entities.UserRole;
import spring.orders.demo.users.entities.UserStatus;

@Tag("integration")
@AutoConfigureMockMvc
class CustomerUserIT extends AbstractIntegrationTestBase {

	private final Logger log = org.slf4j.LoggerFactory.getLogger(CustomerUserIT.class);

	private final String firstUsername = "bobby"; //$NON-NLS-1$
	private final String firstEmail = "bobby@order.processor.com"; //$NON-NLS-1$
	private final String firstAddressLine1 = "NY NY"; //$NON-NLS-1$
	private final String firstPassword = UUID.randomUUID().toString();

	private final String secondUsername = "dan"; //$NON-NLS-1$
	private final String secondEmail = "dan@order.processor.com"; //$NON-NLS-1$
	private final String secondAddressLine1 = "LA LA"; //$NON-NLS-1$
	private final String secondPassword = UUID.randomUUID().toString();

	private final String newEmail = "newemail@order.processor.com"; //$NON-NLS-1$
	private final String newAddressLine1 = "AU TX"; //$NON-NLS-1$

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
		createUser(firstUsername, firstEmail, firstPassword, firstAddressLine1);
		// getting user (+ ADMIN)
		getAllUsers(2);

		createUser(secondUsername, secondEmail, firstPassword, secondAddressLine1);
		// getting both users (+ ADMIN)
		getAllUsers(3);
	}

	@Test
	void updateUsers() throws Exception {
		final CustomerUserResponse newUser = createUser(secondUsername, secondEmail, secondPassword, secondAddressLine1);
		// getting user (+ ADMIN)
		getAllUsers(2);

		final var updateRequest = new UpdateCustomerUserRequest();
		updateRequest.setEmail(newEmail);
		updateRequest.setAddress(new AddressDTO(newAddressLine1));
		updateRequest.setPassword(UUID.randomUUID().toString());
		// FIXME: change put to patch
		final MvcResult result = mockMvc.perform(put(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(Constants.X_USER, Constants.USER_ADMIN)
						.param(Constants.PARAM_EXTERNAL_ID, newUser.getExternalId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(JSON_PATH_EMAIL).value(newEmail))
				.andExpect(jsonPath(JSON_PATH_ADDRESS_LINE1).value(newAddressLine1))
				.andExpect(jsonPath(JSON_PATH_EXTERNAL_ID).value(newUser.getExternalId()))
				.andExpect(jsonPath(JSON_PATH_USERNAME).value(newUser.getUsername()))
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}

		// getting user (+ ADMIN)
		getAllUsers(2);
	}

	@Test
	void deleteUsers() throws Exception {
		final CustomerUserResponse firstUser = createUser(firstUsername, firstEmail, firstPassword, firstAddressLine1);
		// getting user (+ ADMIN)
		getAllUsers(2);

		createUser(secondUsername, secondEmail, secondPassword, secondAddressLine1);
		// getting users (+ ADMIN)
		getAllUsers(3);

		final MvcResult result = mockMvc.perform(delete(Constants.USERS_PATH)
						.param(Constants.PARAM_EXTERNAL_ID, firstUser.getExternalId())
						.header(Constants.X_USER, Constants.USER_ADMIN))
				.andExpect(status().isNoContent())
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}

		// getting user (+ ADMIN)
		final List<CustomerUserResponse> allUsers = getAllUsers(3);
		boolean found = false;
		for (final CustomerUserResponse user : allUsers) {
			if (user.getExternalId().equals(firstUser.getExternalId())) {
				assertEquals(firstUser.getUsername(), user.getUsername(),
						String.format("User %s does not match expected username", user.getExternalId())); //$NON-NLS-1$
				assertEquals(firstUser.getEmail(), user.getEmail(),
						String.format("User %s does not match expected email", user.getExternalId())); //$NON-NLS-1$
				found = true;
				break;
			}
		}
		assertTrue("Deleted user not found", found); //$NON-NLS-1$
	}

	private List<CustomerUserResponse> getAllUsers(int expectedNumberOfUsers) throws Exception {
		final MvcResult result = mockMvc.perform(get(Constants.USERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.X_USER, Constants.USER_ADMIN))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray()) //$NON-NLS-1$
			.andExpect(jsonPath("$.length()").value(expectedNumberOfUsers)) //$NON-NLS-1$
			.andReturn();
		final String content = result.getResponse().getContentAsString();
		if (log.isDebugEnabled()) {
			log.debug(content);
		}
		return objectMapper.readValue(content, new TypeReference<List<CustomerUserResponse>>() {});
	}

	private CustomerUserResponse createUser(String username, String email, String password, String addressLine) throws Exception {
		final var createRequest = new CreateCustomerUserRequest();
		createRequest.setUsername(username);
		createRequest.setEmail(email);
		createRequest.setPassword(password);
		createRequest.setAddress(new AddressDTO(addressLine));

		// first user
		final MvcResult result = mockMvc.perform(post(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(Constants.X_USER, Constants.USER_ADMIN)
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding(StandardCharsets.UTF_8)
						.content(objectMapper.writeValueAsString(createRequest)))
						.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath(JSON_PATH_USERNAME).value(username))
				.andExpect(jsonPath(JSON_PATH_EMAIL).value(email))
				.andExpect(jsonPath(JSON_PATH_ADDRESS_LINE1).value(addressLine))
				.andExpect(jsonPath(JSON_PATH_STATUS).value(UserStatus.ACTIVE))
				.andExpect(jsonPath(JSON_PATH_ROLE).value(UserRole.USER))
				.andExpect(jsonPath(JSON_PATH_EXTERNAL_ID).isNotEmpty())
				.andExpect(jsonPath(JSON_PATH_EXTERNAL_ID, matchesPattern(UUID_REGEX)))
				.andReturn();
		final String content = result.getResponse().getContentAsString();
		if (log.isDebugEnabled()) {
			log.debug(content);
		}
		return objectMapper.readValue(content, new TypeReference<CustomerUserResponse>() {});
	}
}
