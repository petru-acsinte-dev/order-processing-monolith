package spring.orders.demo.ship.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import spring.orders.demo.ship.dto.FulfillmentResponse;
import spring.orders.demo.ship.entities.Fulfillment;
import spring.orders.demo.users.mappers.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface FulfillmentMapper {

	@Mapping(target = "status", source = "fulfillment", qualifiedByName = "mapShipStatus")
	FulfillmentResponse toResponse(Fulfillment fulfillment);

	@Named("mapShipStatus")
	default String mapShipStatus(Fulfillment entity) {
		return entity.getStatus().getStatus();
	}

}
