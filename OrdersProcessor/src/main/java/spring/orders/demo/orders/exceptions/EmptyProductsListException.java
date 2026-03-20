package spring.orders.demo.orders.exceptions;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class EmptyProductsListException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public EmptyProductsListException() {
		super(ApiErrors.EMPTY_PRODUCTS_LIST, MessageKeys.EMPTY_PRODUCTS_LIST);
	}

}
