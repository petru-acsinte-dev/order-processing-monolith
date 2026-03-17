package spring.orders.demo.orders.exceptions;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -3069731091992631404L;

	private final UUID externalId;

	public OrderNotFoundException(UUID externalId) {
		this.externalId = externalId;
	}

	public UUID getExternalId() {
		return externalId;
	}

}
