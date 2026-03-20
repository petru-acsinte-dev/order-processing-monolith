package spring.orders.demo.orders.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import spring.orders.demo.exceptions.ApiError;
import spring.orders.demo.exceptions.ApiErrors;
import spring.orders.demo.orders.exceptions.ProductNotFoundException;

@RestControllerAdvice
public class ProductControllerAdvice {

	@ExceptionHandler(ProductNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleProductNotFound(ProductNotFoundException ex) {
		return new ApiError(ApiErrors.PRODUCT_NOT_FOUND, ex.getMessage());
	}

}
