package spring.orders.demo.users.exceptions;

import org.springframework.http.HttpStatus;

import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.exceptions.ApiException;

public class UserStatusNotFoundException extends ApiException {

	private static final long serialVersionUID = 1L;

	public UserStatusNotFoundException() {
		super(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrors.USER_STATUS_NOT_FOUND, MessageKeys.USER_STATUS_NOT_FOUND);
	}

}
