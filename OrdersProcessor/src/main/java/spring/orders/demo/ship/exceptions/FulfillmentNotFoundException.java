package spring.orders.demo.ship.exceptions;

import java.util.UUID;

public class FulfillmentNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1321425628607242006L;

	private final UUID orderExternalId;

	public FulfillmentNotFoundException(UUID orderExternalId) {
		this.orderExternalId = orderExternalId;
	}

	public UUID getOrderExternalId() {
		return orderExternalId;
	}

}
