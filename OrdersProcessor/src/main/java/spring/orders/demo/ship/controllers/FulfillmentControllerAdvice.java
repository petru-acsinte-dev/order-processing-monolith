package spring.orders.demo.ship.controllers;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import spring.orders.demo.exceptions.ApiError;
import spring.orders.demo.exceptions.ApiException;

@RestControllerAdvice(basePackages = "spring.orders.demo.ship")
public class FulfillmentControllerAdvice {

	private final MessageSource messageSource;

	public FulfillmentControllerAdvice(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException ex, Locale locale) {
		final String message = messageSource.getMessage(
                ex.getMessageKey(),
                ex.getArgs(),
                locale
        );

		final ApiError error = new ApiError(ex.getErrorCode(), message);

		return ResponseEntity
				.status(ex.getStatus())
				.body(error);
	}

}
