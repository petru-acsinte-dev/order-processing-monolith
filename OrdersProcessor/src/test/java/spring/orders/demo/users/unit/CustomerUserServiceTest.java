package spring.orders.demo.users.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.UserRole;
import spring.orders.demo.constants.UserStatus;
import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.entities.Address;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.entities.Role;
import spring.orders.demo.users.entities.Status;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;
import spring.orders.demo.users.mappers.AddressMapper;
import spring.orders.demo.users.mappers.CustomerUserMapper;
import spring.orders.demo.users.repositories.CustomerUserRepository;
import spring.orders.demo.users.repositories.StatusRepository;
import spring.orders.demo.users.services.CustomerUserService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class CustomerUserServiceTest {

	private static final String DAN = "dan"; //$NON-NLS-1$
	private static final String DAN_EMAIL = "dan@dev.com"; //$NON-NLS-1$
	private static final String BOBBY = "bobby"; //$NON-NLS-1$
	private static final String BOBBY_EMAIL = "bobby@dev.com"; //$NON-NLS-1$
	private static final String BAD_PSWD = "1234"; //$NON-NLS-1$
	private static final String ACTIVE = "ACTIVE"; //$NON-NLS-1$

	@Mock
	private CustomerUserRepository userRepository;

	@Mock
	private StatusRepository statusRepository;

	@Mock
	private AddressMapper addressMapper;

	@Mock
	private CustomerUserMapper userMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private CustomerUserService service;

	@Test
	@DisplayName("Retrieves all the users from the system")
	void testFindAllUsers() {
		final CustomerUser admin = getAdminUser();

		given(userRepository.findByUsername(Constants.ADMIN))
			.willReturn(Optional.of(admin));

		given(userRepository.findAll(Pageable.unpaged(Sort.by("username")))) //$NON-NLS-1$
			.willReturn(new PageImpl<>(List.of(admin)));

		mockResponse(admin);

		final List<CustomerUserResponse> all = service.findAllUsers(Constants.ADMIN);

		assertThat(all).size().isEqualTo(1);
		assertThat(all.get(0).getUsername()).isEqualTo(Constants.ADMIN);
	}

	@Test
	@DisplayName("Tests that the admin can create a regular user")
	void testCreateUserAsAdmin() {
		createAs(Constants.ADMIN, BOBBY, BOBBY_EMAIL, BAD_PSWD);
	}

	@Test
	@DisplayName("Tests that a non-admin user cannot create a regular user")
	void testCreateUserAsNonAdmin() {
		createAs(Constants.ADMIN, BOBBY, BOBBY_EMAIL, BAD_PSWD);

		assertThrows(UnauthorizedOperationException.class,
				() -> createAs(BOBBY, DAN, DAN_EMAIL, BAD_PSWD));
	}

	@Test
	@DisplayName("Tests updating the email + address for an existing user")
	void testUpdateUserAsAdmin() {
		final CustomerUser newUser = createAs(Constants.ADMIN, BOBBY, BOBBY_EMAIL, BAD_PSWD);

		updateAs(Constants.ADMIN, newUser, "newbobby@dev.com", new AddressDTO("LA LA"), BAD_PSWD);  //$NON-NLS-1$//$NON-NLS-2$
	}

	@Test
	@DisplayName("Tests deleting an existing user")
	void testDeleteUserAsAdmin() {
		final CustomerUser newUser = createAs(Constants.ADMIN, BOBBY, BOBBY_EMAIL, BAD_PSWD);

		given(userRepository.findByUsername(Constants.ADMIN))
    		.willReturn(Optional.of(getAdminUser()));

		given(userRepository.findByExternalId(newUser.getExternalId()))
			.willReturn(Optional.of(newUser));

		given(statusRepository.findByStatus(UserStatus.ARCHIVED))
			.willReturn(Optional.of(new Status(UserStatus.ARCHIVED_ID, UserStatus.ARCHIVED)));

		service.deleteUser(Constants.ADMIN, newUser.getExternalId());

		verify(userRepository).save(any(CustomerUser.class));
	}

	@Test
	@DisplayName("Tests deleting the admin user")
	void testAdminAsAdmin() {
		final CustomerUser adminUser = getAdminUser();
		given(userRepository.findByUsername(Constants.ADMIN))
    		.willReturn(Optional.of(adminUser));

		assertThrows(UnauthorizedOperationException.class,
				()-> service.deleteUser(Constants.ADMIN, Constants.ADMIN_UUID0));
	}

	@Test
	@DisplayName("Tests that updating as non-admin not possible")
	void testUpdateUserAsNonAdmin() {
		final CustomerUser newUser = createAs(Constants.ADMIN, BOBBY, BOBBY_EMAIL, BAD_PSWD);

		final AddressDTO newAddress = new AddressDTO("LA LA"); //$NON-NLS-1$
		assertThrows(UnauthorizedOperationException.class,
				() -> updateAs(BOBBY, newUser, "newbobby@dev.com", newAddress, BAD_PSWD));  //$NON-NLS-1$
	}

	private void updateAs(String requestorIdentifier, CustomerUser existingUser,
						String email, AddressDTO address, String newPassword) {
		final UpdateCustomerUserRequest updateRequest = new UpdateCustomerUserRequest();
		updateRequest.setEmail(email);
		updateRequest.setAddress(address);
		updateRequest.setPassword(passwordEncoder.encode(newPassword));

		final CustomerUser adminUser = getAdminUser();

		given(userRepository.findByUsername(requestorIdentifier))
        	.willAnswer(invocation -> (Constants.ADMIN.equals(requestorIdentifier) ? Optional.of(adminUser) : Optional.of(existingUser)));

		// this is necessary to avoid UserNotFoundException
		lenient().when(userRepository.findByExternalId(existingUser.getExternalId()))
			.thenReturn(Optional.of(existingUser));

		service.updateUser(requestorIdentifier, existingUser.getExternalId(), updateRequest);

		verify(userRepository).save(any(CustomerUser.class));

	}

	private CustomerUser createAs(String requestorIdentifier, String newUsername, String newEmail, String newPassword) {
		final var createRequest = new CreateCustomerUserRequest();
		createRequest.setUsername(newUsername);
		createRequest.setEmail(newEmail);
		createRequest.setAddress(new AddressDTO("NY NY")); //$NON-NLS-1$
		createRequest.setPassword(newPassword);

		final CustomerUser adminUser = getAdminUser();

		given(passwordEncoder.encode(newPassword))
			.willReturn(staticEncoder.encode(newPassword));

		try (MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            final UUID staticUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); //$NON-NLS-1$
            mockedUUID.when(UUID::randomUUID).thenReturn(staticUUID);

            mockEntity(createRequest);

            final CustomerUser newUser = userMapper.fromCreateRequest(createRequest);
            newUser.setExternalId(staticUUID);

            final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
            newUser.setStatus(status);

            final Role role = new Role(UserRole.USER_ID, UserRole.USER);
            newUser.setRole(role);

            given(userRepository.findByUsername(ArgumentMatchers.anyString()))
	            .willAnswer(invocation -> {
	                final String username = invocation.getArgument(0);
	                return username.equals(Constants.ADMIN) ? Optional.of(adminUser) : Optional.of(newUser);
	            });

            service.createUser(requestorIdentifier, createRequest);

            verify(userRepository).save(any(CustomerUser.class));

            return newUser;
		}
	}

	private void mockEntity(CreateCustomerUserRequest createRequest) {
		final CustomerUser entity = new CustomerUser();
		entity.setUsername(createRequest.getUsername());
		entity.setEmail(createRequest.getEmail());
		entity.setPassword(passwordEncoder.encode(createRequest.getPassword()));

		mockAddress(createRequest.getAddress());

		entity.setAddress(addressMapper.toEntity(createRequest.getAddress()));

		when(userMapper.fromCreateRequest(createRequest))
			.thenReturn(entity);
	}

	private void mockAddress(AddressDTO dto) {
		final Address address = new Address();
		address.setAddressLine1(dto.getAddressLine1());
		final var line2 = dto.getAddressLine2();
		if (null != line2) {
			address.setAddressLine2(line2);
		}

		when(addressMapper.toEntity(dto))
			.thenReturn(address);
	}

	private void mockAddress(Address address) {
		final AddressDTO dto = new AddressDTO(address.getAddressLine1(), address.getAddressLine2());

		when(addressMapper.toDTO(address))
			.thenReturn(dto);
	}

	private void mockResponse(CustomerUser user) {
		mockAddress(user.getAddress());
		final CustomerUserResponse response =
				new CustomerUserResponse(user.getExternalId().toString(),
										user.getUsername(),
										user.getEmail(),
										user.getCreated(),
										user.getRole().getRole(),
										user.getStatus().getStatus(),
										addressMapper.toDTO(user.getAddress()));

		when(userMapper.toResponse(user))
			.thenReturn(response);
	}

	private static PasswordEncoder staticEncoder = new BCryptPasswordEncoder();
	public static CustomerUser getAdminUser() {
		final CustomerUser admin = new CustomerUser();
		admin.setId(0L);
		admin.setUsername(Constants.ADMIN);
		admin.setEmail("admin@order.processing.com"); //$NON-NLS-1$
		admin.setExternalId(Constants.ADMIN_UUID0);
		admin.setCreated(LocalDateTime.now());
		admin.setRole(new Role((short) 0, UserRole.ADMIN));
		admin.setStatus(new Status((short) 0, ACTIVE));
		final Address address = new Address();
		address.setAddressLine1("3401 Hillview Avenue, Palo Alto, CA 94304, USA"); //$NON-NLS-1$
		admin.setAddress(address);
		admin.setPassword(staticEncoder.encode(Constants.ADMIN));
		return admin;
	}

}
