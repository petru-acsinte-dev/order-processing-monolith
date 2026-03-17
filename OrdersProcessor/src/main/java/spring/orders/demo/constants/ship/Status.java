package spring.orders.demo.constants.ship;

public enum Status {

	READY_TO_SHIP( (short) 0),
	CANCELLED( (short) 1),
	SHIPPED( (short) 2);

	private final short id;

	private Status(short id) {
		this.id = id;
	}

	public short getId() {
		return id;
	}
}
