package spring.orders.demo.orders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import spring.orders.demo.orders.dto.OrderLineDTO;
import spring.orders.demo.orders.entities.OrderLine;
import spring.orders.demo.users.mappers.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface OrderLineMapper extends GlobalMapperConfig {

	@Mapping(target = "productExternalId", source = "product.externalId")
	OrderLineDTO toLineDTO(OrderLine line);

}
