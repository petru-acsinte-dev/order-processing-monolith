package spring.orders.demo.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(@JsonProperty("token") String token) {}
