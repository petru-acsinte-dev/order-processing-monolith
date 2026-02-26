package spring.orders.demo.products.dto;

/**
 * Product DTO used for product update requests.
 */
public class UpdateProductRequest extends AbstractProductDTO {
	// cannot update SKU; create new product if SKU needs to change
}
