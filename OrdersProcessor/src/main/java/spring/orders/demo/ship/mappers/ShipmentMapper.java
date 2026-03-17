package spring.orders.demo.ship.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import spring.orders.demo.ship.dto.ShipmentResponse;
import spring.orders.demo.ship.entities.Shipment;
import spring.orders.demo.users.mappers.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface ShipmentMapper {

	@Mapping(target = "status", source = "shipment", qualifiedByName = "mapShipStatus")
	ShipmentResponse toResponse(Shipment shipment);

	@Named("mapShipStatus")
	default String mapShipStatus(Shipment entity) {
		return entity.getStatus().getStatus();
	}
}
