package spring.orders.demo.users.exceptions;

public class UserServiceException extends RuntimeException {

	private static final long serialVersionUID = -679530700364684956L;

	public UserServiceException(String message) {
		super(message);
	}

}
