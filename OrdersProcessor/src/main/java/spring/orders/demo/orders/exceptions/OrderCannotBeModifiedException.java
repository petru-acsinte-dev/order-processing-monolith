package spring.orders.demo.orders.exceptions;

public class OrderCannotBeModifiedException extends RuntimeException {

	private static final long serialVersionUID = 6333528113365626532L;

	private final String orderStatus;

	public OrderCannotBeModifiedException(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

}
