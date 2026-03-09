package spring.orders.demo.users.integration;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.UserStatus;
import spring.orders.demo.shared.AbstractIntegrationTestBase;
import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;

@Transactional
class CustomerUserIT extends AbstractIntegrationTestBase {

	private static final int EXPECTED_SAMPLE_DATA_USERS = 20;

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(CustomerUserIT.class);

	private static final int PAGE_SIZE = 100;

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

	@BeforeEach
	void login() throws Exception {
		loginAs(Constants.ADMIN, Constants.ADMIN);
	}

	@Test
	@DisplayName("Tests retrieving existing users")
	void testGetAllUsers() throws Exception {
		final MvcResult result = mockMvc
				.perform(get(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.param("size", String.valueOf(PAGE_SIZE))) //$NON-NLS-1$
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR).isArray()) //$NON-NLS-1$
				.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR + ".length()") //$NON-NLS-1$ //$NON-NLS-2$
						.value(expectedUsersForThisTest(1)))
				.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR  //$NON-NLS-1$
						+ "[0].username").value(Constants.ADMIN)) //$NON-NLS-1$
				.andReturn();
		if (log.isDebugEnabled()) {
			log.debug(result.getResponse().getContentAsString());
		}
	}

	@Test
	@DisplayName("Tests users creation")
	@Rollback
	void createUsers() throws Exception {
		createAndValidateUser(firstUsername, firstEmail, firstPassword, firstAddressLine1);
		// getting user (+ ADMIN)
		getAllUsers(expectedUsersForThisTest(2));

		createAndValidateUser(secondUsername, secondEmail, firstPassword, secondAddressLine1);
		// getting both users (+ ADMIN)
		getAllUsers(expectedUsersForThisTest(3));
	}

	private int expectedUsersForThisTest(int expectedByTest) {
		return expectedByTest + EXPECTED_SAMPLE_DATA_USERS;
	}

	@Test
	@DisplayName("Tests updating existing users")
	@Rollback
	void updateUsers() throws Exception {
		final CustomerUserResponse newUser = createAndValidateUser(secondUsername, secondEmail, secondPassword, secondAddressLine1);
		// getting user (+ ADMIN)
		getAllUsers(expectedUsersForThisTest(2));

		final var updateRequest = new UpdateCustomerUserRequest();
		updateRequest.setEmail(newEmail);
		updateRequest.setAddress(new AddressDTO(newAddressLine1));
		updateRequest.setPassword(UUID.randomUUID().toString());
		final MvcResult result = mockMvc.perform(patch(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
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
		getAllUsers(expectedUsersForThisTest(2));
	}

	@Test
	@DisplayName("Tests deleting existing users")
	@Rollback
	void deleteUsers() throws Exception {
		final CustomerUserResponse firstUser = createAndValidateUser(firstUsername, firstEmail, firstPassword, firstAddressLine1);
		// getting user (+ ADMIN)
		getAllUsers(expectedUsersForThisTest(2));

		createAndValidateUser(secondUsername, secondEmail, secondPassword, secondAddressLine1);
		// getting users (+ ADMIN)
		getAllUsers(expectedUsersForThisTest(3));

		mockMvc.perform(delete(Constants.USERS_PATH)
						.param(Constants.PARAM_EXTERNAL_ID, firstUser.getExternalId())
						.header(HttpHeaders.AUTHORIZATION, getBearer()))
				.andExpect(status().isNoContent())
				.andReturn();

		// getting user (+ ADMIN)
		final List<CustomerUserResponse> allUsers = getAllUsers(expectedUsersForThisTest(3));
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

	@Test
	@DisplayName("Tests deleting admin user")
	@Rollback
	void deleteAdmin() throws Exception {
		mockMvc.perform(delete(Constants.USERS_PATH)
						.param(Constants.PARAM_EXTERNAL_ID, Constants.ADMIN_UUID0.toString())
						.header(HttpHeaders.AUTHORIZATION, getBearer()))
				.andExpect(status().isForbidden());

		final List<CustomerUserResponse> allUsers = getAllUsers(expectedUsersForThisTest(1));
		boolean active = false;
		for (final CustomerUserResponse user : allUsers) {
			if (Constants.ADMIN_UUID0.toString().equals(user.getExternalId())) {
				assertEquals(Constants.ADMIN, user.getUsername(),
						String.format("User %s does not match expected username", user.getExternalId())); //$NON-NLS-1$
				assertEquals(UserStatus.ACTIVE, user.getStatus(),
						String.format("User %s does not match expected status", user.getExternalId())); //$NON-NLS-1$
				active = true;
				break;
			}
		}
		assertTrue("Deleted user not found", active); //$NON-NLS-1$
	}

	@Override
	protected Logger getLog() {
		return log;
	}

	private List<CustomerUserResponse> getAllUsers(int expectedNumberOfUsers) throws Exception {
		final MvcResult result = mockMvc.perform(get(Constants.USERS_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, getBearer())
				.param("size", String.valueOf(PAGE_SIZE))) //$NON-NLS-1$
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$." + Constants.PAGE_CONTENT_ATTR).isArray()) //$NON-NLS-1$
			.andExpect(jsonPath(String.format("$.%s.length()",  //$NON-NLS-1$
					Constants.PAGE_CONTENT_ATTR)).value(expectedNumberOfUsers))
			.andReturn();
		final String content = result.getResponse().getContentAsString();
		if (log.isDebugEnabled()) {
			log.debug(content);
		}
		final JsonNode root = objectMapper.readTree(content);
		final JsonNode contentNode = root.get(Constants.PAGE_CONTENT_ATTR);
		return objectMapper.convertValue(contentNode, new TypeReference<List<CustomerUserResponse>>() {});
	}

}
