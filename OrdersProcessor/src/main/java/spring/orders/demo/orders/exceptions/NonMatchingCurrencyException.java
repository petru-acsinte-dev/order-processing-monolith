package spring.orders.demo.orders.exceptions;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class NonMatchingCurrencyException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public NonMatchingCurrencyException(String expected, String actual) {
		super(ApiErrors.INCOMPATIBLE_CURRENCIES, MessageKeys.INCOMPATIBLE_CURRENCIES, expected, actual);
	}

}
