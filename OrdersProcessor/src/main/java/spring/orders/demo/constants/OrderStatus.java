package spring.orders.demo.constants;

public class OrderStatus {
	private OrderStatus() {}

	public static final String CREATED = "CREATED"; //$NON-NLS-1$
	public static final short CREATED_ID = 0;
    public static final String CANCELLED = "CANCELLED"; //$NON-NLS-1$
    public static final short CANCELLED_ID = 1;
    public static final String SHIPPED = "SHIPPED"; //$NON-NLS-1$
    public static final short SHIPPED_ID = 2;
    public static final String CONFIRMED = "CONFIRMED"; //$NON-NLS-1$
    public static final short CONFIRMED_ID = 3;
}
