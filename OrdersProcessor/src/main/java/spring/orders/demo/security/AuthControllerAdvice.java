package spring.orders.demo.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import spring.orders.demo.users.exceptions.ApiError;
import spring.orders.demo.users.exceptions.ApiErrors;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;

@RestControllerAdvice
public class AuthControllerAdvice {

	@ExceptionHandler(UnauthorizedOperationException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	/**
	 * This covers all the inaccessible services that throw {@link UnauthorizedOperationException}
	 */
	public ApiError handleUnauthorizedOperation(UnauthorizedOperationException uoe) {
		return new ApiError(ApiErrors.UNAUTHORIZED_OPERATION, uoe.getMessage());
	}

}
