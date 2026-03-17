package spring.orders.demo.orders.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import spring.orders.demo.orders.exceptions.EmptyProductsListException;
import spring.orders.demo.orders.exceptions.IncompatibleProductCurrencies;
import spring.orders.demo.orders.exceptions.OrderCannotBeModifiedException;
import spring.orders.demo.orders.exceptions.TooManyProductsInRequest;
import spring.orders.demo.users.exceptions.ApiError;
import spring.orders.demo.users.exceptions.ApiErrors;

@RestControllerAdvice
public class OrderControllerAdvice {

	@ExceptionHandler(EmptyProductsListException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleEmptyListOnCreate(EmptyProductsListException empty) {
		return new ApiError(ApiErrors.INCORRECT_INPUT, "The products list cannot be empty");
	}

	@ExceptionHandler(IncompatibleProductCurrencies.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleIncompatibleCurrencies(IncompatibleProductCurrencies ex) {
		return new ApiError(ApiErrors.INCOMPATIBLE_CURRENCIES,
				String.format("%s and %s currencies are not compatible",
						ex.getOrderCurrency().getDisplayName(),
						ex.getProductCurrency().getDisplayName()));
	}

	@ExceptionHandler(TooManyProductsInRequest.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleRequestTooBig(TooManyProductsInRequest tooBig) {
		return new ApiError(ApiErrors.REQUEST_TOO_BIG,
				String.format("Request size %s exceed %s limit",
						tooBig.getRequestSize(),
						tooBig.getSystemMax()));
	}

	@ExceptionHandler(OrderCannotBeModifiedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleOrderCannotBeModified(OrderCannotBeModifiedException dont) {
		return new ApiError(ApiErrors.ORDER_STATUS_DOES_NOT_ALLOW_OP,
				String.format("Order %s is %s", dont.getExternalId(), dont.getOrderStatus()));
	}

}
