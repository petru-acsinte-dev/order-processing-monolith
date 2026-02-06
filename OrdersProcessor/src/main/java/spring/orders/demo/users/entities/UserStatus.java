package spring.orders.demo.users.entities;

public class UserStatus {
	private UserStatus() {}

	public static final String ACTIVE = "ACTIVE"; //$NON-NLS-1$
	public static final short ACTIVE_ID = 0;
    public static final String LOCKED = "LOCKED"; //$NON-NLS-1$
    public static final short LOCKED_ID = 1;
    public static final String ARCHIVED = "ARCHIVED"; //$NON-NLS-1$
    public static final short ARCHIVED_ID = 2;

}
