package spring.orders.demo.products.entities;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import spring.orders.demo.products.exceptions.NonMatchingCurrencyException;

@Embeddable
@Access(AccessType.FIELD)
public class Money {

	@NotNull
	@PositiveOrZero
	@Column(name = "amount", precision = 19, scale = 4)
	private BigDecimal amount;

	@NotNull
	private Currency currency; // ISO 4217 code

	protected Money() {}

	private Money(@NotNull @PositiveOrZero BigDecimal amount, @NotNull Currency currency) {
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Amount cannot be negative"); //$NON-NLS-1$
		}
		this.amount = amount;
		this.currency = currency;
	}

	public static Money of(BigDecimal amount, Currency currency) {
		return new Money(amount, currency);
	}

	public BigDecimal amount() {
		return amount;
	}

	public Currency currency() {
		return currency;
	}

	// no setters; immutable

	public Money add(Money money) {
		checkCurrency(money);
		return Money.of(amount.add(money.amount), currency);
	}

	public Money multiply(Money money) {
		checkCurrency(money);
		return Money.of(amount.multiply(money.amount()), currency);
	}

	private void checkCurrency(Money money) {
		if (null == money) {
			throw new IllegalArgumentException("Money cannot be null"); //$NON-NLS-1$
		}
		if (currency.equals(money.currency())) {
			return;
		}
		throw new NonMatchingCurrencyException(currency.getCurrencyCode(), money.currency.getCurrencyCode());
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, currency);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final Money other = (Money) obj;
		return Objects.equals(amount, other.amount) && Objects.equals(currency, other.currency);
	}

}
