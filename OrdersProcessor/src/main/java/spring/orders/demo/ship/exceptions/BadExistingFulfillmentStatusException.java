package spring.orders.demo.ship.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class BadExistingFulfillmentStatusException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public BadExistingFulfillmentStatusException(UUID orderExternalId, String status) {
		super (	ApiErrors.BAD_EXISTING_FULFILLMENT_STATUS,
				MessageKeys.BAD_EXISTING_FULFILLMENT_STATUS,
				orderExternalId,
				status
		);
	}

}
