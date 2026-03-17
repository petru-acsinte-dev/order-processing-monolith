package spring.orders.demo.ship.exceptions;

import java.util.UUID;

public class FulfillmentNotFound extends RuntimeException {

	private static final long serialVersionUID = -8747150265744867846L;

	private final UUID externalOrderId;

	public FulfillmentNotFound(UUID externalOrderId) {
		this.externalOrderId = externalOrderId;
	}

	public UUID getExternalOrderId() {
		return externalOrderId;
	}

}
