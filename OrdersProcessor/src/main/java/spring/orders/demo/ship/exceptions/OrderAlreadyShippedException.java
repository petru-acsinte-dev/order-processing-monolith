package spring.orders.demo.ship.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class OrderAlreadyShippedException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public OrderAlreadyShippedException(UUID orderExternalId) {
		super(ApiErrors.ORDER_ALREADY_SHIPPED, MessageKeys.ORDER_ALREADY_SHIPPED, orderExternalId);
	}

}
