package spring.orders.demo.products.mappers;

import org.mapstruct.ObjectFactory;

import spring.orders.demo.products.dto.MoneyDTO;
import spring.orders.demo.products.entities.Money;

interface MoneyFactory {

	@ObjectFactory
	default Money toMoney(MoneyDTO dto) {
		if (null == dto) {
			return null;
		}
		return Money.of(dto.getAmount(), dto.getCurrency());
	}

}
