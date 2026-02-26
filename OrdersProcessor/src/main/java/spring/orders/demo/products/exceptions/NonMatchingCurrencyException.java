package spring.orders.demo.products.exceptions;

import jakarta.validation.constraints.NotBlank;

public class NonMatchingCurrencyException extends RuntimeException {

	public NonMatchingCurrencyException(@NotBlank String expected, @NotBlank String actual) {
		super(String.format("%s does not match expected %s currency", actual, expected)); //$NON-NLS-1$
	}

	private static final long serialVersionUID = 4296762256913171430L;

}
