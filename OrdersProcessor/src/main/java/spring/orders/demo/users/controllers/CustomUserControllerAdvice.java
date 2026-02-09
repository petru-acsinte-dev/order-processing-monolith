package spring.orders.demo.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;
import spring.orders.demo.users.exceptions.ApiError;
import spring.orders.demo.users.exceptions.ApiErrors;
import spring.orders.demo.users.exceptions.DuplicateUserException;
import spring.orders.demo.users.exceptions.UserNotFoundException;
import spring.orders.demo.users.exceptions.UserServiceException;

@Hidden // breaks swagger UI
@RestControllerAdvice
public class CustomUserControllerAdvice {

	@ExceptionHandler(DuplicateUserException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleDuplicateUser(DuplicateUserException dex) {
		return new ApiError(ApiErrors.DUPLICATE_USER, dex.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleUserNotFound(UserNotFoundException nfe) {
		return new ApiError(ApiErrors.USER_NOT_FOUND, nfe.getMessage());
	}

	@ExceptionHandler(UserServiceException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleUserServiceException(UserServiceException ex) {
		return new ApiError(ApiErrors.USER_SERVICE_ERROR, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleIllegalArgumentException(IllegalArgumentException ex) {
		return new ApiError(ApiErrors.INCORRECT_INPUT, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleGenericException(Exception e) {
		return new ApiError(ApiErrors.INTERNAL_ERROR, e.getMessage());
	}

}
