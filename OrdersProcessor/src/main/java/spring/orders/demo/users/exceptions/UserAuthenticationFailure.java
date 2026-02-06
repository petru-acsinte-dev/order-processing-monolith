package spring.orders.demo.users.exceptions;

public class UserAuthenticationFailure extends RuntimeException {

	public UserAuthenticationFailure() {
	}

	public UserAuthenticationFailure(String message) {
		super(message);
	}

	private static final long serialVersionUID = 8201614069523783079L;

}
