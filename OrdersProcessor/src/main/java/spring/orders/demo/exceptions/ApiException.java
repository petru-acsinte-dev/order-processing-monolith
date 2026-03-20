package spring.orders.demo.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final ApiErrors errorCode;
	private final HttpStatus status;
	private final String messageKey;
	private final transient Object[] args;

	protected ApiException(	HttpStatus status,
							ApiErrors errorCode,
							String messageKey,
							Object... args) {
		this.status = status;
		this.errorCode = errorCode;
		this.messageKey = messageKey;
		this.args = args;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public ApiErrors getErrorCode() {
		return errorCode;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getArgs() {
		return args;
	}
}
