package spring.orders.demo.users.services;

import java.util.List;
import java.util.UUID;

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
import spring.orders.demo.users.exceptions.UserAuthenticationFailure;
import spring.orders.demo.users.exceptions.UserNotFoundException;
import spring.orders.demo.users.mappers.AddressMapper;
import spring.orders.demo.users.mappers.CustomerUserMapper;
import spring.orders.demo.users.repositories.CustomerUserRepository;

@Service
public class CustomerUserService {

	private final CustomerUserRepository repository;

	public CustomerUserService(CustomerUserRepository repository) {
		this.repository = repository;
	}

	@Transactional (readOnly = true)
	public CustomerUserResponse login(String identifier) {
		final CustomerUser user = repository.findByEmail(identifier)
				.or(() -> repository.findByUsername(identifier))
				.orElseThrow(UserAuthenticationFailure::new);

		checkStatus(user);
		throw new UserAuthenticationFailure(
				String.format("%s could not be used to identify an existing, active user. "
						+ "Only username or email accepted as input", identifier));
	}

	private void checkStatus(CustomerUser user) {
		final String userStatus = user.getStatus().getStatus();
		if ( ! UserStatus.ACTIVE.equals(userStatus)) {
			throw new UserAuthenticationFailure(String.format("User %s (%s) is %s", user.getUsername(), user.getEmail(), userStatus));
		}
	}

	/**
	 * Admin level service.
	 * @return A collection of existing users ordered by username.
	 */
	@Transactional (readOnly = true)
	public List<CustomerUserResponse> findAll() {
		final Page<CustomerUser> users = repository.findAll(Pageable.unpaged(Sort.by("username"))); // TODO: unpaged for now
		return users.stream().map(CustomerUserMapper::responseFrom).toList();
	}

	@Transactional
	public void createUser(CreateCustomerUserRequest createRequest) {
		final CustomerUser partialEntity = CustomerUserMapper.fromRequest(createRequest);
		partialEntity.setExternalId(UUID.randomUUID());
		final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
		partialEntity.setStatus(status);
		final Role role = new Role(UserRole.USER_ID, UserRole.USER);
		partialEntity.setRole(role);
		repository.save(partialEntity);
	}

	/**
	 * Updates the user email and/or address.
	 * @param userUpdateRequest User update request.
	 */
	@Transactional
	public void updateUser(UUID externalId, UpdateCustomerUserRequest userUpdateRequest) {
		final CustomerUser user = repository.findByExternalId(externalId).orElseThrow(UserNotFoundException::new);
		if (null != userUpdateRequest.getEmail()) {
			user.setEmail(userUpdateRequest.getEmail());
		}
		if (null != userUpdateRequest.getAddress()) {
			user.setAddress(AddressMapper.toEntity(userUpdateRequest.getAddress()));
		}
		repository.save(user);
	}

	/**
	 * Marks the user as deleted. The user is not completely removed from the database because past orders might reference it.
	 * @param externalId External user identifier (UUID)
	 */
	@Transactional
	public void deleteUser(UUID externalId) {
		final CustomerUser user = repository.findByExternalId(externalId).orElseThrow(UserNotFoundException::new);
		final Status status = user.getStatus();
		status.setStatus(UserStatus.ARCHIVED);
		user.setStatus(status);
		repository.save(user);
	}
}
