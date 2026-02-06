package spring.orders.demo.users.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.entities.Role;
import spring.orders.demo.users.entities.Status;
import spring.orders.demo.users.entities.UserRole;
import spring.orders.demo.users.entities.UserStatus;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;
import spring.orders.demo.users.exceptions.UserAuthenticationFailure;
import spring.orders.demo.users.exceptions.UserNotFoundException;
import spring.orders.demo.users.mappers.AddressMapper;
import spring.orders.demo.users.mappers.CustomerUserMapper;
import spring.orders.demo.users.repositories.CustomerUserRepository;

@Service
public class CustomerUserService {

	private static Logger log = org.slf4j.LoggerFactory.getLogger(CustomerUserService.class);

	private final CustomerUserRepository repository;

	public CustomerUserService(CustomerUserRepository repository) {
		this.repository = repository;
	}

	/**
	 * Used to simulate a login, by identifying the user.
	 * @param piiIdentifier Personal identifier (username <- recommended or email)
	 * @return Identified user, if it exits.
	 */
	@Transactional (readOnly = true)
	public CustomerUserResponse login(String piiIdentifier) {
		log.debug("login(): Identifying user in the system"); //$NON-NLS-1$
		final CustomerUser user = getUserByPiiIdentifier(piiIdentifier);

		checkUserStatus(user);

		final CustomerUserResponse response = CustomerUserMapper.responseFrom(user);
		log.info("User {} logged in", user.getId()); //$NON-NLS-1$
		return response;
	}

	private CustomerUser getUserByPiiIdentifier(String piiIdentifier) {
		return repository.findByUsername(piiIdentifier)
				.or(() -> repository.findByEmail(piiIdentifier))
				.orElseThrow(UserAuthenticationFailure::new);
	}

	private static void checkUserStatus(CustomerUser user) {
		log.debug("Checking status for user {}", user.getId()); //$NON-NLS-1$
		final String userStatus = user.getStatus().getStatus();
		if ( ! UserStatus.ACTIVE.equals(userStatus)) {
			log.warn("User {} status is {}", user.getId(), userStatus); //$NON-NLS-1$
			throw new UserAuthenticationFailure("User is not active"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the existing users. Requires admin role.
	 * @param requestorIdentifier PII identifier for the requestor (username <- recommended or email)
	 * @return A collection of existing users ordered by username.
	 */
	@Transactional (readOnly = true)
	public List<CustomerUserResponse> findAllUsers(String requestorIdentifier) {
		log.debug("findAllUsers(): Identifying the requestor"); //$NON-NLS-1$
		checkIfAdmin(requestorIdentifier);

		log.debug("Listing all users (admin)"); //$NON-NLS-1$
		// TODO: unpaged for now
		final Page<CustomerUser> users = repository.findAll(Pageable.unpaged(Sort.by("username"))); //$NON-NLS-1$
		return users.stream().map(CustomerUserMapper::responseFrom).toList();
	}

	private void checkIfAdmin(String requestorIdentifier) {
		final CustomerUser requestor = getUserByPiiIdentifier(requestorIdentifier);
		if ( ! UserRole.ADMIN.equals(requestor.getRole().getRole())) {
			log.warn("Forbidden: User {} is not an admin user", requestor.getId()); //$NON-NLS-1$
			throw new UnauthorizedOperationException();
		}
	}

	/**
	 * Creates a new user. Requires admin role.
	 * @param requestorIdentifier PII identifier for the admin user making the request (username <- recommended or email)
	 * @param createRequest New users details wrapped in {@link CreateCustomerUserRequest}
	 */
	@Transactional
	public void createUser(String requestorIdentifier, CreateCustomerUserRequest createRequest) {
		log.debug("Creating new user..."); //$NON-NLS-1$
		checkIfAdmin(requestorIdentifier);

		final CustomerUser partialEntity = CustomerUserMapper.fromRequest(createRequest);

		final UUID newExternalId = UUID.randomUUID();
		partialEntity.setExternalId(newExternalId);

		final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
		partialEntity.setStatus(status);

		final Role role = new Role(UserRole.USER_ID, UserRole.USER);
		partialEntity.setRole(role);

		log.debug("Saving new user..."); //$NON-NLS-1$
		repository.save(partialEntity);
		log.info("User {} created successfully", newExternalId); //$NON-NLS-1$
	}

	/**
	 * Updates the user email and/or address for an existing user. Requires admin role.
	 * @param requestorIdentifier PII identifier for an admin user making the request (username <- recommended or email)
	 * @param externalId External identifier of user to be updated (UUID)
	 */
	@Transactional
	public void updateUser(String requestorIdentifier, UUID externalId, UpdateCustomerUserRequest userUpdateRequest) {
		checkIfAdmin(requestorIdentifier);

		log.debug("updateUser(): Finding user with external id {}", externalId); //$NON-NLS-1$
		final CustomerUser user = repository.findByExternalId(externalId).orElseThrow(UserNotFoundException::new);
		if (null != userUpdateRequest.getEmail()) {
			user.setEmail(userUpdateRequest.getEmail());
		}
		if (null != userUpdateRequest.getAddress()) {
			user.setAddress(AddressMapper.toEntity(userUpdateRequest.getAddress()));
		}

		log.debug("Updating user {}", user.getId()); //$NON-NLS-1$
		repository.save(user);
		log.info("User {} updated", user.getId()); //$NON-NLS-1$
	}

	/**
	 * Marks the user as deleted. The user is not completely removed from the database because past orders might reference it.
	 * Requires admin role.
	 * @param requestorIdentifier PII identifier for an admin user making the request (username <- recommended or email)
	 * @param externalId External identifier of user to be deleted (UUID)
	 */
	@Transactional
	public void deleteUser(String requestorIdentifier, UUID externalId) {
		checkIfAdmin(requestorIdentifier);

		log.debug("deleteUser(): Finding user with external id {}", externalId); //$NON-NLS-1$
		final CustomerUser user = repository.findByExternalId(externalId).orElseThrow(UserNotFoundException::new);
		final Status status = user.getStatus();
		status.setStatus(UserStatus.ARCHIVED);
		user.setStatus(status);

		log.debug("Deleting user {}", user.getId()); //$NON-NLS-1$
		repository.save(user);
		log.info("User {} marked as deleted", user.getId()); //$NON-NLS-1$
	}
}
