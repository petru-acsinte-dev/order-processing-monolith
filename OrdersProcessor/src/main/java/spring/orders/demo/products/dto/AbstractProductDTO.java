package spring.orders.demo.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public abstract class AbstractProductDTO {

	@NotBlank
	@Schema(description = "Unique product identifier",
			example = "SKU-107435")
	private String sku;

	@NotBlank
	@Schema(example = "Logitech K380 Keyboard")
	private String name;

	@Schema(example = "Compact multi-device Bluetooth keyboard")
	private String description;

	@Schema(description = "Indicates if the product is active and not discontinued")
	private boolean active;

	@Valid
	private MoneyDTO cost;

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public MoneyDTO getCost() {
		return cost;
	}

	public void setCost(MoneyDTO cost) {
		this.cost = cost;
	}

}
