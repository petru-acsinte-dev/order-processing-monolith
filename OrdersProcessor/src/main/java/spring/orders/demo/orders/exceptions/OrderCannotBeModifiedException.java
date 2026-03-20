package spring.orders.demo.orders.exceptions;

import java.util.UUID;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.BadRequestApiException;

public class OrderCannotBeModifiedException extends BadRequestApiException {

	private static final long serialVersionUID = 6333528113365626532L;

	public OrderCannotBeModifiedException(UUID externalId, String orderStatus) {
		super(ApiErrors.ORDER_STATUS_DOES_NOT_ALLOW_OP, MessageKeys.CANNOT_MODIFY_ORDER, externalId, orderStatus);
	}

}
