package spring.orders.demo.orders.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.NotFoundApiException;

public class OrderNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public OrderNotFoundException(UUID externalId) {
		super(ApiErrors.ORDER_NOT_FOUND, MessageKeys.ORDER_NOT_FOUND, externalId);
	}

}
