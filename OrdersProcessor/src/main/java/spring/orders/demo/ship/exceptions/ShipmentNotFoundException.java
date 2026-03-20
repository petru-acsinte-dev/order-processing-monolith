package spring.orders.demo.ship.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.NotFoundApiException;

public class ShipmentNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public ShipmentNotFoundException(UUID externalOrderId) {
		super(ApiErrors.SHIPMENT_NOT_FOUND, MessageKeys.SHIPMENT_NOT_FOUND, externalOrderId);
	}

}
