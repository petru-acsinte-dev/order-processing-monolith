package spring.orders.demo.users.mappers;

import org.mapstruct.Mapper;

import spring.orders.demo.users.dto.AddressDTO;
import spring.orders.demo.users.entities.Address;

@Mapper(config = GlobalMapperConfig.class)
public interface AddressMapper {

	Address toEntity(AddressDTO dto);

	AddressDTO toDTO(Address entity);

}
