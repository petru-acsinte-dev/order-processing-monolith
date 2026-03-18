package spring.orders.demo.ship.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import spring.orders.demo.ship.exceptions.BadExistingFulfillmentStatusException;
import spring.orders.demo.ship.exceptions.FulfillmentNotFoundException;
import spring.orders.demo.users.exceptions.ApiError;
import spring.orders.demo.users.exceptions.ApiErrors;

@RestControllerAdvice
public class FulfillmentControllerAdvice {

	@ExceptionHandler(FulfillmentNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleFulfillmentNotFound(FulfillmentNotFoundException nf) {
		return new ApiError(ApiErrors.FULFILLMENT_NOT_FOUND, nf.getOrderExternalId().toString());
	}

	@ExceptionHandler(BadExistingFulfillmentStatusException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleFulfillmentNotFound(BadExistingFulfillmentStatusException bad) {
		return new ApiError(ApiErrors.FULFILLMENT_NOT_FOUND, bad.getOrderExternalId().toString());
	}
}
