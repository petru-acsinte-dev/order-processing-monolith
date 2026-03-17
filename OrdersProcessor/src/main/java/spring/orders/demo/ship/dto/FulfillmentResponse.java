package spring.orders.demo.ship.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used to provide information about an order fulfillment.
 */
public class FulfillmentResponse {

	@NotNull
	@Schema(description = "The fulfillment unique external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID externalId;

	@NotNull
	@Schema(description = "The order unique external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID orderExternalId;

	@NotBlank
	@Schema(description = "The fulfillment status",
			example = "One of READY_TO_SHIP, CANCELLED, SHIPPED")
	private String status;

	@NotNull
	@Schema(description = "The fulfillment creation date")
	private LocalDateTime created;

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

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

}
