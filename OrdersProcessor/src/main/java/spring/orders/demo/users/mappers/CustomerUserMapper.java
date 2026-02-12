package spring.orders.demo.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.entities.CustomerUser;

@Mapper(config = GlobalMapperConfig.class, uses = AddressMapper.class)
public interface CustomerUserMapper {

	@Mapping(source = "externalId", 	target = "externalId")
    @Mapping(source = "role.role", 		target = "role")
    @Mapping(source = "status.status", 	target = "status")
    CustomerUserResponse toResponse(CustomerUser entity);

	@Mapping(target = "id", 		ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "created", 	ignore = true)
    @Mapping(target = "role", 		ignore = true)
    @Mapping(target = "status", 	ignore = true)
	CustomerUser fromCreateRequest(CreateCustomerUserRequest request);

	@Mapping(target = "id", 		ignore = true)
	@Mapping(target = "username", 	ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "created", 	ignore = true)
    @Mapping(target = "role", 		ignore = true)
    @Mapping(target = "status", 	ignore = true)
	void updateRequest(UpdateCustomerUserRequest request,
					@MappingTarget CustomerUser customerUser);
}
