package spring.orders.demo.ship.exceptions;

import java.util.UUID;

public class OrderAlreadyShippedException extends RuntimeException {

	private static final long serialVersionUID = 5529164651824419061L;

	private final UUID orderExternalId;

	public OrderAlreadyShippedException(UUID orderExternalId) {
		this.orderExternalId = orderExternalId;
	}

	public UUID getOrderExternalId() {
		return orderExternalId;
	}

}
