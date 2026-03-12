package spring.orders.demo.orders.exceptions;

public class TooManyProductsInRequest extends RuntimeException {

	private static final long serialVersionUID = -8082185862767178403L;

	private final int systemMax;

	private final int requestSize;

	public TooManyProductsInRequest(int systemMax, int requestSize) {
		this.systemMax = systemMax;
		this.requestSize = requestSize;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getSystemMax() {
		return systemMax;
	}

	public int getRequestSize() {
		return requestSize;
	}

}
