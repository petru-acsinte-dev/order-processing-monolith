package spring.orders.demo.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ForbiddenApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected ForbiddenApiException(ApiErrors errorCode, String messageKey, Object... args) {
		super(HttpStatus.FORBIDDEN, errorCode, messageKey, args);
	}

}
