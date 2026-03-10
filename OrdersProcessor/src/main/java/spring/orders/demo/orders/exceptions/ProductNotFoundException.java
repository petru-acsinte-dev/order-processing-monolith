package spring.orders.demo.orders.exceptions;

public class ProductNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -1426307569738958672L;

	public ProductNotFoundException() {
	}

	public ProductNotFoundException(String message) {
		super(message);
	}

}
