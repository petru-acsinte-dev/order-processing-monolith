package spring.orders.demo.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Product DTO used to provide information and identification for a specific product.
 */
public class ProductResponse extends AbstractProductDTO {

	@NotBlank
	@Schema(description = "Unique product external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private String externalId;

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

}
