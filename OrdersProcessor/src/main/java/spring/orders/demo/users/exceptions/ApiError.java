package spring.orders.demo.users.exceptions;

import java.time.Instant;

public record ApiError(ApiErrors status, String message, Instant time) {

}
