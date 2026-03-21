package spring.orders.demo.ship.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "shipments", schema = "ship")
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "external_id", unique = true, nullable = false)
    private UUID externalId;

	@NotNull
	@Column(name = "order_external_id", unique = true, nullable = false)
    private UUID orderExternalId;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", referencedColumnName = "id")
    private ShipStatus status;

    @NotNull
    private LocalDateTime shipped;

    @PrePersist
    public void prePersist() {
        if (this.shipped == null) {
            this.shipped = LocalDateTime.now();
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public ShipStatus getStatus() {
		return status;
	}

	public void setStatus(ShipStatus status) {
		this.status = status;
	}

	public LocalDateTime getShipped() {
		return shipped;
	}

	public void setShipped(LocalDateTime shipped) {
		this.shipped = shipped;
	}

}
