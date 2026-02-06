package spring.orders.demo.users.mappers;

import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.entities.CustomerUser;

public class CustomerUserMapper {
	private CustomerUserMapper() {
		/* This utility class should not be instantiated */
	}

	/**
	 * Maps a {@link CustomerUser} entity to a {@link CustomerUserResponse} DTO.
	 * @param entity {@link CustomerUser}
	 * @return {@link CustomerUserResponse}
	 */
	public static CustomerUserResponse responseFrom(CustomerUser entity) {
		return new CustomerUserResponse(entity.getExternalId().toString(),
				entity.getUsername(),
				entity.getEmail(),
				entity.getCreated(),
				entity.getRole().getRole(),
				entity.getStatus().getStatus(),
				AddressMapper.from(entity.getAddress()));
	}

	/**
	 * Maps a {@link CreateCustomerUserRequest} DTO to a partially constructed {@link CustomerUser} entity.
	 * Required status and role attributes are not set.
	 * @param request {@link CreateCustomerUserRequest}
	 * @return {@link CustomerUser}
	 */
	public static CustomerUser fromRequest(CreateCustomerUserRequest request) {
		final CustomerUser entity = new CustomerUser();
		entity.setEmail(request.getEmail());
		entity.setUsername(request.getUsername());
		entity.setAddress(AddressMapper.toEntity(request.getAddress()));
		return entity;
	}

}
