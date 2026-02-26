package spring.orders.demo.users.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
import spring.orders.demo.users.mappers.AddressMapper;
import spring.orders.demo.users.mappers.AddressMapperImpl;
import spring.orders.demo.users.mappers.CustomerUserMapper;
import spring.orders.demo.users.mappers.CustomerUserMapperImpl;

@Tag("mapper")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AddressMapperImpl.class, CustomerUserMapperImpl.class})
class UserMapperTest {

	@Autowired
	private AddressMapper addressMapper;

	@Autowired
	private CustomerUserMapper userMapper;

	@Test
	void testMappers() {
		final String addressLine = "NY NY"; //$NON-NLS-1$
		final Address address = new Address();
		address.setAddressLine1(addressLine);

		final AddressDTO addressDTO = addressMapper.toDTO(address);
		assertEquals("Address line not matching", addressLine, addressDTO.getAddressLine1()); //$NON-NLS-1$
		assertNull(addressDTO.getAddressLine2());

		final Address entity = addressMapper.toEntity(addressDTO);
		assertEquals("Address line not matching", addressLine, entity.getAddressLine1()); //$NON-NLS-1$
		assertNull(entity.getAddressLine2());

		final String email = "john@dev.com"; //$NON-NLS-1$
		final String name = "Johnny"; //$NON-NLS-1$
		final UUID uuid = UUID.randomUUID();
		final Role role = new Role(UserRole.USER_ID, UserRole.USER);
		final Status status = new Status(UserStatus.ACTIVE_ID, UserStatus.ACTIVE);
		final LocalDateTime created = LocalDateTime.now();
		{
			final CustomerUser user = new CustomerUser();
			user.setId(1L);
			user.setUsername(name);
			user.setAddress(address);
			user.setCreated(created);
			user.setEmail(email);
			user.setExternalId(uuid);
			user.setRole(role);
			user.setStatus(status);

			final CustomerUserResponse response = userMapper.toResponse(user);
			assertEquals(name, response.getUsername());
			assertEquals(email, response.getEmail());
			assertEquals(uuid.toString(), response.getExternalId());
			assertEquals(created, response.getCreated());
			assertEquals(UserStatus.ACTIVE, response.getStatus());
			assertEquals(UserRole.USER, response.getRole());
			assertEquals(addressDTO, response.getAddress());
		}

		{
			final CreateCustomerUserRequest create = new CreateCustomerUserRequest();
			create.setAddress(addressDTO);
			create.setEmail(email);
			create.setUsername(name);

			final CustomerUser newUser = userMapper.fromCreateRequest(create);
			assertEquals(name, newUser.getUsername());
			assertEquals(email, newUser.getEmail());
			assertEquals(addressLine, newUser.getAddress().getAddressLine1());
			assertNull(newUser.getAddress().getAddressLine2());
			assertNull(newUser.getId());
			assertNull(newUser.getExternalId());
			assertNull(newUser.getCreated());
			assertNull(newUser.getStatus());
			assertNull(newUser.getRole());
		}

		final UpdateCustomerUserRequest update = new UpdateCustomerUserRequest();
		update.setEmail(email);
		update.setAddress(addressDTO);
		final CustomerUser updatedUser = new CustomerUser();
		userMapper.updateRequest(update, updatedUser);
		assertEquals(email, updatedUser.getEmail());
		assertEquals(addressLine, updatedUser.getAddress().getAddressLine1());
		assertNull(updatedUser.getAddress().getAddressLine2());
		assertNull(updatedUser.getId());
		assertNull(updatedUser.getExternalId());
		assertNull(updatedUser.getCreated());
		assertNull(updatedUser.getStatus());
		assertNull(updatedUser.getRole());
	}

}
