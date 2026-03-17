package spring.orders.demo.ship.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used to provide information about an order shipment.
 */
public class ShipmentResponse {

	@NotNull
	@Schema(description = "The shipment unique external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID externalId;

	@NotNull
	@Schema(description = "The order unique external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID orderExternalId;

	@NotBlank
	@Schema(description = "The shipment status",
			example = "SHIPPED")
	private String status;

	@NotNull
	@Schema(description = "The shipment date")
	private LocalDateTime shipped;

	public UUID getExternalId() {
		return externalId;
	}

	public void setExternalId(UUID externalId) {
		this.externalId = externalId;
	}

	public UUID getOrderExternalId() {
		return orderExternalId;
	}

	public void setOrderExternalId(UUID orderExternalId) {
		this.orderExternalId = orderExternalId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getShipped() {
		return shipped;
	}

	public void setShipped(LocalDateTime shipped) {
		this.shipped = shipped;
	}

}
