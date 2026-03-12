package spring.orders.demo.orders.exceptions;

import java.util.Currency;

public class IncompatibleProductCurrencies extends RuntimeException {

	private static final long serialVersionUID = -1376679426785025421L;
	private final Currency orderCurrency;
	private final Currency productCurrency;

	public IncompatibleProductCurrencies(Currency orderCurrency, Currency productCurrency) {
		this.orderCurrency = orderCurrency;
		this.productCurrency = productCurrency;
	}

	public Currency getOrderCurrency() {
		return orderCurrency;
	}

	public Currency getProductCurrency() {
		return productCurrency;
	}

}
