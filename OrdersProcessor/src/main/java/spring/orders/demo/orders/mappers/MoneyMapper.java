package spring.orders.demo.orders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import spring.orders.demo.orders.dto.MoneyDTO;
import spring.orders.demo.orders.entities.Money;
import spring.orders.demo.users.mappers.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface MoneyMapper extends GlobalMapperConfig, MoneyFactory {

	@Mapping(target = "add", ignore = true)
	@Mapping(target = "multiply", ignore = true)
	Money toEntity(MoneyDTO dto);

	MoneyDTO toDTO(Money entity);

}
