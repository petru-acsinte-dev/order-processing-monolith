package spring.orders.demo.ship.exceptions;

import java.util.UUID;

public class BadExistingFulfillmentStatus extends RuntimeException {

	private static final long serialVersionUID = -1776252884236197100L;

	private final String status;

	private final UUID orderExternalId;

	public BadExistingFulfillmentStatus(UUID orderExternalId, String status) {
		this.orderExternalId = orderExternalId;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public UUID getOrderExternalId() {
		return orderExternalId;
	}

}
