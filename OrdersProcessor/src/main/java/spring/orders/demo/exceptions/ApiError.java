package spring.orders.demo.exceptions;

import java.time.Instant;

public record ApiError(ApiErrors status, String message, Instant time) {

	public ApiError(ApiErrors status, String message) {
		this(status, message, Instant.now());
	}

}
