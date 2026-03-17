package spring.orders.demo.orders;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "orders.demo.ship")
public class ShipProps {

	private int pageSize;

	private int maxPageSize;

	private String shipmentSortingAttribute;

	private String fulfillmentSortingAttribute;

	/**
	 * @return The page size used for listing products, orders, order lines
	 */
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return Maximum customizable limit for a page size.
	 * Cannot exceed {@link Constants.PAGE_SIZE_HARD_LIMIT}
	 */
	public int getMaxPageSize() {
		return maxPageSize;
	}

	public void setMaxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
	}

	public String getShipmentSortingAttribute() {
		return shipmentSortingAttribute;
	}

	public void setShipmentSortingAttribute(String shipmentSortingAttribute) {
		this.shipmentSortingAttribute = shipmentSortingAttribute;
	}

	public String getFulfillmentSortingAttribute() {
		return fulfillmentSortingAttribute;
	}

	public void setFulfillmentSortingAttribute(String fulfillmentSortingAttribute) {
		this.fulfillmentSortingAttribute = fulfillmentSortingAttribute;
	}

}
