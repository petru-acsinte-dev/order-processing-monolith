package spring.orders.demo.users.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.UserRole;
import spring.orders.demo.constants.UserStatus;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.users.UserProps;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.entities.Role;
import spring.orders.demo.users.entities.Status;
import spring.orders.demo.users.exceptions.DuplicateUserException;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;
import spring.orders.demo.users.exceptions.UserNotFoundException;
import spring.orders.demo.users.exceptions.UserServiceException;
import spring.orders.demo.users.exceptions.UserStatusNotFoundException;
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

	private final UserProps userProps;

	public CustomerUserService(CustomerUserRepository userRepository,
								StatusRepository statusRepository,
								CustomerUserMapper userMapper,
								AddressMapper addressMapper,
								PasswordEncoder passwordEncoder,
								UserProps userProps) {
		this.userRepository = userRepository;
		this.statusRepository = statusRepository;
		this.userMapper = userMapper;
		this.addressMapper = addressMapper;
		this.passwordEncoder = passwordEncoder;
		this.userProps = userProps;
	}

	/**
	 * Returns the existing users. Requires admin role.
	 * @return A collection of existing users ordered by username.
	 */
	@Transactional (readOnly = true)
	public Page<CustomerUserResponse> findUsers(Pageable pageable) {
		SecurityUtils.confirmAdminRole();

		final Pageable request = getPagingRequest(pageable);

		log.debug("Listing existing users"); //$NON-NLS-1$
		final Page<CustomerUser> users = userRepository.findAll(request);
		return users.map(userMapper::toResponse);
	}

	/**
	 * Creates a new user. Requires admin role.
	 * @param createRequest New users details wrapped in {@link CreateCustomerUserRequest}
	 * @return The newly created user information.
	 */
	@Transactional
	public CustomerUserResponse createUser(CreateCustomerUserRequest createRequest) {
		log.debug("Creating new user..."); //$NON-NLS-1$
		SecurityUtils.confirmAdminRole();

		final CustomerUser partialEntity = userMapper.fromCreateRequest(createRequest);

		final UUID newExternalId = UUID.randomUUID();
		partialEntity.setExternalId(newExternalId);

		final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
		partialEntity.setStatus(status);

		final Role role = new Role(UserRole.USER_ID, UserRole.USER);
		partialEntity.setRole(role);

		partialEntity.setCreated(LocalDateTime.now());

		partialEntity.setPassword(passwordEncoder.encode(createRequest.getPassword()));

		log.debug("Saving new user..."); //$NON-NLS-1$
		final CustomerUser newUser;
		try {
			newUser = userRepository.save(partialEntity);
		} catch (final DataIntegrityViolationException ex) {
			log.warn("{} encountered whilst creating user {}", ex.getClass().getCanonicalName(), newExternalId); //$NON-NLS-1$
			throw new DuplicateUserException(partialEntity.getUsername(), partialEntity.getEmail());
		} catch (final Exception e) {
			throw new UserServiceException(e);
		}
		log.info("User {} created successfully", newExternalId); //$NON-NLS-1$
		return userMapper.toResponse(newUser);
	}

	/**
	 * Updates the user email and/or address for an existing user. Requires admin role.
	 * @param externalId External identifier of user to be updated (UUID)
	 */
	@Transactional
	public CustomerUserResponse updateUser(UUID externalId, UpdateCustomerUserRequest userUpdateRequest) {
		SecurityUtils.confirmAdminRole();

		log.debug("updateUser(): Finding user with external id {}", externalId); //$NON-NLS-1$
		final CustomerUser user = userRepository.findByExternalId(externalId).orElseThrow(() -> new UserNotFoundException(externalId));
		try {
			if (null != userUpdateRequest.getEmail()) {
				user.setEmail(userUpdateRequest.getEmail());
			}
			if (null != userUpdateRequest.getAddress()) {
				user.setAddress(addressMapper.toEntity(userUpdateRequest.getAddress()));
			}
			if (null != userUpdateRequest.getPassword()) {
				user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
			}
			return userMapper.toResponse(user);
		} catch (final DataIntegrityViolationException ex) {
			log.warn("{} encountered whilst updating user {}", //$NON-NLS-1$
					ex.getClass().getCanonicalName(), user.getExternalId());
			throw new DuplicateUserException(user.getUsername(), user.getEmail());
		} catch (final Exception e) {
			throw new UserServiceException(e);
		}
	}

	/**
	 * Marks the user as deleted. The user is not completely removed from the database because past orders might reference it.
	 * Requires admin role.
	 * @param externalId External identifier of user to be deleted (UUID)
	 */
	@Transactional
	public void deleteUser(UUID externalId) {
		SecurityUtils.confirmAdminRole();

		if (Constants.ADMIN_UUID0.equals(externalId)) {
			log.info("Admin user cannot be deleted"); //$NON-NLS-1$
			throw new UnauthorizedOperationException();
		}

		log.debug("deleteUser(): Finding user with external id {}", externalId); //$NON-NLS-1$
		final CustomerUser user = userRepository.findByExternalId(externalId)
												.orElseThrow(() -> new UserNotFoundException(externalId));

		final Status archivedStatus = statusRepository.findByStatus(UserStatus.ARCHIVED)
				.orElseThrow(UserStatusNotFoundException::new);

		log.debug("Deleting user {}", user.getId()); //$NON-NLS-1$
		user.setStatus(archivedStatus);
		log.info("User {} marked as deleted", user.getId()); //$NON-NLS-1$
	}

	private Pageable getPagingRequest(Pageable pageable) {
		int pageNo = 0;
		int pageSize = userProps.getPageSize();
		Sort sortBy = null == userProps.getDefaultSortAttribute() ?
				null : Sort.by(userProps.getDefaultSortAttribute());
		if (null != pageable) {
			if (pageable.getPageNumber() > 0) {
				pageNo = pageable.getPageNumber();
			}
			final int requestSize = pageable.getPageSize();
			if (requestSize > 0
			&& requestSize <= Constants.PAGE_SIZE_HARD_LIMIT // system imposed limit
			&& requestSize <= userProps.getMaxPageSize()) {
				pageSize = requestSize;
			}
			if (null == sortBy) {
				sortBy = pageable.getSort();
			} else {
				sortBy = pageable.getSortOr(sortBy);
			}
		}
		if (null == sortBy) {
			return PageRequest.of(pageNo, pageSize);
		}
		return PageRequest.of(pageNo, pageSize, sortBy);
	}

}
