package spring.orders.demo.orders.exceptions;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class TooManyProductsInRequest extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public TooManyProductsInRequest(int systemMax, int requestSize) {
		super(ApiErrors.REQUEST_TOO_BIG, MessageKeys.REQUEST_TOO_BIG, systemMax, requestSize);
	}

}
