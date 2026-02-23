package spring.orders.demo.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthRequest(
		@JsonProperty("username") String username,
		@JsonProperty("password") String password) {}
