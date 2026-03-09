package spring.orders.demo.orders;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "orders.demo.orders")
public class OrderProps {

	private int pageSize;

	private int maxPageSize;

	private String defaultSortAttribute;

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

	/**
	 * @return Default attribute to sort by
	 */
	public String getDefaultSortAttribute() {
		return defaultSortAttribute;
	}

	public void setDefaultSortAttribute(String defaultSortAttribute) {
		this.defaultSortAttribute = defaultSortAttribute;
	}

}
