package spring.orders.demo.orders.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Order DTO used to provide information and identification for a specific order.
 */
public class OrderResponse {

	@NotNull
	@Schema(description = "The order unique external identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID externalId;

	@NotNull
	@Schema(description = "The unique external identifier for the order owner",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID customerExternalId;

	@NotBlank
	@Schema(description = "The order status",
			example = "One of CREATED, CANCELLED, CONFIRMED, SHIPPED")
	private String status;

	@NotNull
	@Schema(description = "The order creation date")
	private LocalDateTime created;

	@NotNull
	@Schema(description = "The products added to the order")
	private List<OrderLineDTO> orderLines;

	@Embedded
	private MoneyDTO orderTotal;

	public UUID getExternalId() {
		return externalId;
	}

	public void setExternalId(UUID externalId) {
		this.externalId = externalId;
	}

	public UUID getCustomerExternalId() {
		return customerExternalId;
	}

	public void setCustomerExternalId(UUID customerExternalId) {
		this.customerExternalId = customerExternalId;
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

	public List<OrderLineDTO> getOrderLines() {
		return orderLines;
	}

	public void setOrderLines(List<OrderLineDTO> orderLines) {
		this.orderLines = orderLines;
	}

	public MoneyDTO getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(MoneyDTO orderTotal) {
		this.orderTotal = orderTotal;
	}

}
