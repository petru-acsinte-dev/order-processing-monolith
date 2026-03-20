package spring.orders.demo.exceptions;

import org.springframework.http.HttpStatus;

public abstract class BadRequestApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected BadRequestApiException(ApiErrors errorCode, String messageKey, Object... args) {
		super(HttpStatus.BAD_REQUEST, errorCode, messageKey, args);
	}

}
