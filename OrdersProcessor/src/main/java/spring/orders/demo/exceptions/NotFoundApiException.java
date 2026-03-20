package spring.orders.demo.exceptions;

import org.springframework.http.HttpStatus;

public abstract class NotFoundApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected NotFoundApiException(ApiErrors errorCode, String messageKey, Object... args) {
		super(HttpStatus.NOT_FOUND, errorCode, messageKey, args);
	}

}
