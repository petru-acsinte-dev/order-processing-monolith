package spring.orders.demo.users.exceptions;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.ForbiddenApiException;

public class UnauthorizedOperationException extends ForbiddenApiException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedOperationException() {
		super(ApiErrors.UNAUTHORIZED_OPERATION, MessageKeys.UNAUTHORIZED_OPERATION);
	}

}
