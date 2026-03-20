package spring.orders.demo.orders.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class UnknownOrderStatusException extends BadRequestApiException {

	private static final long serialVersionUID = 1L;

	public UnknownOrderStatusException(UUID externalId, String orderStatus) {
		super(ApiErrors.ORDER_STATUS_DOES_NOT_ALLOW_OP, MessageKeys.CANNOT_MODIFY_ORDER, externalId, orderStatus);
	}

}
