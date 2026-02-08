package spring.orders.demo.users.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
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

import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.entities.Address;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.entities.Role;
import spring.orders.demo.users.entities.Status;
import spring.orders.demo.users.entities.UserRole;
import spring.orders.demo.users.entities.UserStatus;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;
import spring.orders.demo.users.mappers.CustomerUserMapper;
import spring.orders.demo.users.repositories.CustomerUserRepository;

@ExtendWith(MockitoExtension.class)
class CustomerUserServiceTest {

	private static final String UUID0 = "00000000-0000-0000-0000-000000000000"; //$NON-NLS-1$
	private static final String ADMIN = "ADMIN"; //$NON-NLS-1$
	private static final String ACTIVE = "ACTIVE"; //$NON-NLS-1$

	@Mock
	private CustomerUserRepository repository;

	@InjectMocks
	private CustomerUserService service;

	@Test
	@DisplayName("Checks that the admin user can login")
	void testAdminLogin() {
		final CustomerUser admin = getAdminUser();

		given(repository.findByUsername(ADMIN))
			.willReturn(Optional.of(admin));

		final CustomerUserResponse response = service.login(ADMIN);

		assertThat(response).extracting("username", "role", "status")   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
							.containsExactly(ADMIN, ADMIN, ACTIVE);
	}

	@Test
	@DisplayName("Retrieves all the users from the system")
	void testFindAllUsers() {
		final CustomerUser admin = getAdminUser();

		given(repository.findByUsername(ADMIN))
			.willReturn(Optional.of(admin));

		given(repository.findAll(Pageable.unpaged(Sort.by("username")))) //$NON-NLS-1$
			.willReturn(new PageImpl<>(List.of(admin)));

		final List<CustomerUserResponse> all = service.findAllUsers(ADMIN);

		assertThat(all).size().isEqualTo(1);
		assertThat(all.get(0).getUsername()).isEqualTo(ADMIN);
	}

	@Test
	@DisplayName("Tests that the admin can create a regular user")
	void testCreateUserAsAdmin() {
		createAs(ADMIN, "bobby", "bobby@dev.com");  //$NON-NLS-1$//$NON-NLS-2$
	}

	@Test
	@DisplayName("Tests that a non-admin user cannot create a regular user")
	void testCreateUserAsNonAdmin() {
		createAs(ADMIN, "bobby", "bobby@dev.com");  //$NON-NLS-1$//$NON-NLS-2$

		assertThrows(UnauthorizedOperationException.class,
				() -> createAs("bobby", "dan", "dan@dev.com"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	@DisplayName("Tests updating the email + address for an existing user")
	void testUpdateUserAsAdmin() {
		final CustomerUser newUser = createAs(ADMIN, "bobby", "bobby@dev.com");  //$NON-NLS-1$//$NON-NLS-2$

		updateAs(ADMIN, newUser, "newbobby@dev.com", new AddressDTO("LA LA"));  //$NON-NLS-1$//$NON-NLS-2$
	}

	@Test
	@DisplayName("Tests deleting an existing user")
	void testDeleteUserAsAdmin() {
		final CustomerUser newUser = createAs(ADMIN, "bobby", "bobby@dev.com");  //$NON-NLS-1$//$NON-NLS-2$

		given(repository.findByUsername(ADMIN))
    		.willReturn(Optional.of(getAdminUser()));

		given(repository.findByExternalId(newUser.getExternalId()))
			.willReturn(Optional.of(newUser));

		service.deleteUser(ADMIN, newUser.getExternalId());

		// create + delete
		verify(repository, times(2)).save(any(CustomerUser.class));
	}

	@Test
	@DisplayName("Tests that updating as non-admin not possible")
	void testUpdateUserAsNonAdmin() {
		final CustomerUser newUser = createAs(ADMIN, "bobby", "bobby@dev.com");  //$NON-NLS-1$//$NON-NLS-2$

		final AddressDTO newAddress = new AddressDTO("LA LA"); //$NON-NLS-1$
		assertThrows(UnauthorizedOperationException.class,
				() -> updateAs("bobby", newUser, "newbobby@dev.com", newAddress));  //$NON-NLS-1$//$NON-NLS-2$
	}

	private void updateAs(String requestorIdentifier, CustomerUser existingUser, String email, AddressDTO address) {
		final UpdateCustomerUserRequest updateRequest = new UpdateCustomerUserRequest();
		updateRequest.setEmail(email);
		updateRequest.setAddress(address);

		final CustomerUser adminUser = getAdminUser();

		given(repository.findByUsername(requestorIdentifier))
        	.willAnswer(invocation -> (ADMIN.equals(requestorIdentifier) ? Optional.of(adminUser) : Optional.of(existingUser)));

		// this is necessary to avoid UserNotFoundException
		lenient().when(repository.findByExternalId(existingUser.getExternalId()))
			.thenReturn(Optional.of(existingUser));

		service.updateUser(requestorIdentifier, existingUser.getExternalId(), updateRequest);

		// create + update
		verify(repository, times(2)).save(any(CustomerUser.class));

	}

	private CustomerUser createAs(String requestorIdentifier, String newUsername, String newEmail) {
		final var createRequest = new CreateCustomerUserRequest();
		createRequest.setUsername(newUsername);
		createRequest.setEmail(newEmail);
		createRequest.setAddress(new AddressDTO("NY NY")); //$NON-NLS-1$

		final CustomerUser adminUser = getAdminUser();

		try (MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            final UUID staticUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); //$NON-NLS-1$
            mockedUUID.when(UUID::randomUUID).thenReturn(staticUUID);

            final CustomerUser newUser = CustomerUserMapper.fromRequest(createRequest);
            newUser.setExternalId(staticUUID);

            final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
            newUser.setStatus(status);

            final Role role = new Role(UserRole.USER_ID, UserRole.USER);
            newUser.setRole(role);

            given(repository.findByUsername(ArgumentMatchers.anyString()))
	            .willAnswer(invocation -> {
	                final String username = invocation.getArgument(0);
	                return username.equals(ADMIN) ? Optional.of(adminUser) : Optional.of(newUser);
	            });

            service.createUser(requestorIdentifier, createRequest);

            verify(repository).save(any(CustomerUser.class));

            return newUser;
		}
	}

	private static CustomerUser getAdminUser() {
		final CustomerUser admin = new CustomerUser();
		admin.setId(0L);
		admin.setUsername(ADMIN);
		admin.setEmail("admin@order.processing.com"); //$NON-NLS-1$
		admin.setExternalId(UUID.fromString(UUID0));
		admin.setCreated(LocalDateTime.now());
		admin.setRole(new Role((short) 0, ADMIN));
		admin.setStatus(new Status((short) 0, ACTIVE));
		final Address address = new Address();
		address.setAddressLine1("3401 Hillview Avenue, Palo Alto, CA 94304, USA"); //$NON-NLS-1$
		admin.setAddress(address);
		return admin;
	}

}
