package spring.orders.demo.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthError(@JsonProperty("error") String error,
						@JsonProperty("message") String message) {

	@Override
	public String toString() {
		return "{\"error\":\"" + error  //$NON-NLS-1$
				+ "\",\"message\":\"" + message + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
