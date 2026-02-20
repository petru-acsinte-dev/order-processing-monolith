package spring.orders.demo.users.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import spring.orders.demo.users.exceptions.DuplicateUserException;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;
import spring.orders.demo.users.exceptions.UserAuthenticationFailure;
import spring.orders.demo.users.exceptions.UserNotFoundException;
import spring.orders.demo.users.exceptions.UserServiceException;
import spring.orders.demo.users.mappers.AddressMapper;
import spring.orders.demo.users.mappers.CustomerUserMapper;
import spring.orders.demo.users.repositories.CustomerUserRepository;
import spring.orders.demo.users.repositories.StatusRepository;

@Service
public class CustomerUserService {

	private static Logger log = org.slf4j.LoggerFactory.getLogger(CustomerUserService.class);

	private final CustomerUserRepository userRepository;

	private final StatusRepository statusRepository;

	private final AddressMapper addressMapper;

	private final CustomerUserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	public CustomerUserService(CustomerUserRepository userRepository,
								StatusRepository statusRepository,
								CustomerUserMapper userMapper,
								AddressMapper addressMapper,
								PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.statusRepository = statusRepository;
		this.userMapper = userMapper;
		this.addressMapper = addressMapper;
		this.passwordEncoder = passwordEncoder;
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

		final CustomerUserResponse response = userMapper.toResponse(user);
		log.info("User {} logged in", user.getId()); //$NON-NLS-1$
		return response;
	}

	private CustomerUser getUserByPiiIdentifier(String piiIdentifier) {
		return userRepository.findByUsername(piiIdentifier)
				.or(() -> userRepository.findByEmail(piiIdentifier))
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
		// FIXME: unpaged for now
		final Page<CustomerUser> users = userRepository.findAll(Pageable.unpaged(Sort.by("username"))); //$NON-NLS-1$
		return users.stream().map(userMapper::toResponse).toList();
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
	 * @return The newly created user information.
	 */
	@Transactional
	public CustomerUserResponse createUser(String requestorIdentifier, CreateCustomerUserRequest createRequest) {
		log.debug("Creating new user..."); //$NON-NLS-1$
		checkIfAdmin(requestorIdentifier);

		final CustomerUser partialEntity = userMapper.fromCreateRequest(createRequest);

		final UUID newExternalId = UUID.randomUUID();
		partialEntity.setExternalId(newExternalId);

		final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
		partialEntity.setStatus(status);

		final Role role = new Role(UserRole.USER_ID, UserRole.USER);
		partialEntity.setRole(role);

		partialEntity.setCreated(LocalDateTime.now());

		log.debug("Saving new user..."); //$NON-NLS-1$
		final CustomerUser newUser;
		try {
			newUser = userRepository.save(partialEntity);
		} catch (final DataIntegrityViolationException ex) {
			log.warn("{} encountered whilst creating user {}", ex.getClass().getCanonicalName(), newExternalId); //$NON-NLS-1$
			throw new DuplicateUserException(String.format("User '%s' (%s) already exists",  //$NON-NLS-1$
					partialEntity.getUsername(), partialEntity.getEmail()));
		} catch (final Exception e) {
			log.warn("{} encountered during user creation", e.getClass().getCanonicalName()); //$NON-NLS-1$
			throw new UserServiceException("The user creation encountered an exception"); //$NON-NLS-1$
		}
		log.info("User {} created successfully", newExternalId); //$NON-NLS-1$
		return userMapper.toResponse(newUser);
	}

	/**
	 * Updates the user email and/or address for an existing user. Requires admin role.
	 * @param requestorIdentifier PII identifier for an admin user making the request (username <- recommended or email)
	 * @param externalId External identifier of user to be updated (UUID)
	 */
	@Transactional
	public CustomerUserResponse updateUser(String requestorIdentifier, UUID externalId, UpdateCustomerUserRequest userUpdateRequest) {
		checkIfAdmin(requestorIdentifier);

		log.debug("updateUser(): Finding user with external id {}", externalId); //$NON-NLS-1$
		final CustomerUser user = userRepository.findByExternalId(externalId).orElseThrow(UserNotFoundException::new);
		if (null != userUpdateRequest.getEmail()) {
			user.setEmail(userUpdateRequest.getEmail());
		}
		if (null != userUpdateRequest.getAddress()) {
			user.setAddress(addressMapper.toEntity(userUpdateRequest.getAddress()));
		}
		if (null != userUpdateRequest.getPassword()) {
			user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
		}

		final CustomerUser updatedUser;
		try {
			log.debug("Updating user {}", user.getId()); //$NON-NLS-1$
			updatedUser = userRepository.save(user);
			log.info("User {} updated", user.getId()); //$NON-NLS-1$
			return userMapper.toResponse(updatedUser);
		} catch (final DataIntegrityViolationException ex) {
			log.warn("{} encountered whilst updating user {}", ex.getClass().getCanonicalName(), user.getExternalId()); //$NON-NLS-1$
			throw new DuplicateUserException(String.format("User '%s' (%s) already exists",  //$NON-NLS-1$
					user.getUsername(), user.getEmail()));
		} catch (final Exception e) {
			log.warn("{} encountered during user updaate", e.getClass().getCanonicalName()); //$NON-NLS-1$
			throw new UserServiceException("The user update encountered an exception"); //$NON-NLS-1$
		}
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
		final CustomerUser user = userRepository.findByExternalId(externalId).orElseThrow(UserNotFoundException::new);

		final Status archivedStatus = statusRepository.findByStatus(UserStatus.ARCHIVED)
				.orElseThrow(InvalidUserStatusException::new);

		log.debug("Deleting user {}", user.getId()); //$NON-NLS-1$
		user.setStatus(archivedStatus);
		log.info("User {} marked as deleted", user.getId()); //$NON-NLS-1$
	}
}
