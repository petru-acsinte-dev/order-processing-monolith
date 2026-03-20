package spring.orders.demo.ship.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.NotFoundApiException;

public class FulfillmentNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public FulfillmentNotFoundException(UUID orderExternalId) {
		super(ApiErrors.FULFILLMENT_NOT_FOUND, MessageKeys.FULFILLMENT_NOT_FOUND, orderExternalId);
	}

}
