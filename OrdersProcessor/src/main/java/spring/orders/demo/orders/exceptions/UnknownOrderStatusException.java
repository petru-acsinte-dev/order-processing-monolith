package spring.orders.demo.orders.exceptions;

import java.util.UUID;

public class UnknownOrderStatusException extends OrderCannotBeModifiedException {

	private static final long serialVersionUID = 5273833721485052990L;

	public UnknownOrderStatusException(UUID externalId, String orderStatus) {
		super(externalId, orderStatus);
	}

}
