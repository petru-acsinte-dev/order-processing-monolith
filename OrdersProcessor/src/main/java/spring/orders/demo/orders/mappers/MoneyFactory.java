package spring.orders.demo.orders.mappers;

import org.mapstruct.ObjectFactory;

import spring.orders.demo.orders.dto.MoneyDTO;
import spring.orders.demo.orders.entities.Money;

interface MoneyFactory {

	@ObjectFactory
	default Money toMoney(MoneyDTO dto) {
		if (null == dto) {
			return null;
		}
		return Money.of(dto.getAmount(), dto.getCurrency());
	}

}
