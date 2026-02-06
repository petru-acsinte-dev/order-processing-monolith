package spring.orders.demo.users.mappers;

import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.entities.Address;

public class AddressMapper {
	private AddressMapper() {
		/* This utility class should not be instantiated */
	}

	public static AddressDTO from(Address entity) {
		return new AddressDTO(entity.getAddressLine1(), entity.getAddressLine2());
	}

	public static Address toEntity(AddressDTO dto) {
		final Address address = new Address();
		address.setAddressLine1(dto.getAddressLine1());
		address.setAddressLine2(dto.getAddressLine2());
		return address;
	}

}
