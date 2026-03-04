package spring.orders.demo;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spring.orders.demo.shared.SharedPostgresContainer.POSTGRES;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.UserRole;
import spring.orders.demo.constants.UserStatus;
import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTestBase {

	protected static final String UUID_REGEX = "[a-fA-F0-9]{8}-([a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}"; //$NON-NLS-1$

	protected static final String JSON_PATH_EXTERNAL_ID = "$.externalId"; //$NON-NLS-1$
	protected static final String JSON_PATH_ROLE = "$.role"; //$NON-NLS-1$
	protected static final String JSON_PATH_STATUS = "$.status"; //$NON-NLS-1$
	protected static final String JSON_PATH_ADDRESS_LINE1 = "$.address.addressLine1"; //$NON-NLS-1$
	protected static final String JSON_PATH_EMAIL = "$.email"; //$NON-NLS-1$
	protected static final String JSON_PATH_USERNAME = "$.username"; //$NON-NLS-1$

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl); //$NON-NLS-1$
        registry.add("spring.datasource.username", POSTGRES::getUsername); //$NON-NLS-1$
        registry.add("spring.datasource.password", POSTGRES::getPassword); //$NON-NLS-1$
    }

	private String bearer;

	public String getBearer() {
		return bearer;
	}

	/**
	 * Performs login for the specified credentials and retrieves a JWT token.
	 * @param username Name of existing user.
	 * @param password Password of existing user.
	 * @throws Exception
	 */
	protected void loginAs(String username, String password) throws Exception {
		final MvcResult result = mockMvc.perform(post(Constants.LOGIN_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				// {"username":"x","password":"y"} payload
				.content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}",  //$NON-NLS-1$
						username, password)))
			.andExpect(status().isOk())
			.andReturn();
		final String strContent = result.getResponse().getContentAsString();
		final String token = JsonPath.read(strContent, "$.token"); //$NON-NLS-1$
		bearer = Constants.BEARER + token;
	}

	protected abstract Logger getLog();

	/**
	 * Helper method to create additional users for tests.
	 * @param username Unique username.
	 * @param email Unique email.
	 * @param password Password.
	 * @param addressLine Required address part.
	 * @return The {@link CustomerUserResponse} representing the new user.
	 * @throws Exception
	 */
	protected CustomerUserResponse createAndValidateUser(String username, String email, String password, String addressLine) throws Exception {
		final var createRequest = new CreateCustomerUserRequest();
		createRequest.setUsername(username);
		createRequest.setEmail(email);
		createRequest.setPassword(password);
		createRequest.setAddress(new AddressDTO(addressLine));

		// first user
		final MvcResult result = mockMvc.perform(post(Constants.USERS_PATH)
						.accept(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, getBearer())
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding(StandardCharsets.UTF_8)
						.content(objectMapper.writeValueAsString(createRequest)))
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
		if (getLog().isDebugEnabled()) {
			getLog().debug(content);
		}
		return objectMapper.readValue(content, new TypeReference<CustomerUserResponse>() {});
	}
}
