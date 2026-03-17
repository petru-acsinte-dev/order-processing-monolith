package spring.orders.demo.orders.exceptions;

import java.util.UUID;

public class OrderCannotBeModifiedException extends RuntimeException {

	private static final long serialVersionUID = 6333528113365626532L;

	private final String orderStatus;

	private final UUID externalId;

	public OrderCannotBeModifiedException(UUID externalId, String orderStatus) {
		this.externalId = externalId;
		this.orderStatus = orderStatus;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public UUID getExternalId() {
		return externalId;
	}

}
