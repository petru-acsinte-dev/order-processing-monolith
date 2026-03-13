package spring.orders.demo.orders.exceptions;

public class OrderNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -3069731091992631404L;

	public OrderNotFoundException() {
	}

	public OrderNotFoundException(String message) {
		super(message);
	}

}
