package spring.orders.demo.ship.exceptions;

import java.util.UUID;

public class ShipmentNotFound extends RuntimeException {

	private static final long serialVersionUID = -6990899007786042942L;

	private final UUID externalOrderId;

	public ShipmentNotFound(UUID externalOrderId) {
		this.externalOrderId = externalOrderId;
	}

	public UUID getExternalOrderId() {
		return externalOrderId;
	}

}
