package spring.orders.demo.products.dto;

import java.math.BigDecimal;
import java.util.Currency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MoneyDTO {

	@NotNull
	@PositiveOrZero
	private final BigDecimal amount;

	@NotNull
	@Schema(description = "ISO 4217 currency symbol", example = "USD, CAD, EUR etc.")
	private final Currency currency;

	public MoneyDTO(BigDecimal amount, Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Currency getCurrency() {
		return currency;
	}

}
