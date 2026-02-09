package spring.orders.demo.users.exceptions;

public class DuplicateUserException extends RuntimeException {

	private static final long serialVersionUID = 7305673160640510339L;

	public DuplicateUserException(String msg) {
		super(msg);
	}

}
