package spring.orders.demo.orders.exceptions;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.NotFoundApiException;

public class ProductNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public ProductNotFoundException() {
		super(ApiErrors.PRODUCT_NOT_FOUND, MessageKeys.PRODUCT_NOT_FOUND);
	}

}
