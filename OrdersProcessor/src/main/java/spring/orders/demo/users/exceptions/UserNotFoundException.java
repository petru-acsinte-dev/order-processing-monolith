package spring.orders.demo.users.exceptions;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.NotFoundApiException;

public class UserNotFoundException extends NotFoundApiException {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(Object identifier) {
		super(ApiErrors.USER_NOT_FOUND, MessageKeys.USER_NOT_FOUND, identifier);
	}

}
