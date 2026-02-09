package spring.orders.demo.users.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table( name = "status", schema = "users")
public class Status {

	@Id
	private Short id;

	@NotBlank
	@Column (nullable = false, unique = true)
	private String status;

	protected Status() {}

	public Status(Short id, String status) {
		this.id = id;
		this.status = status;
	}

	public Short getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	// no setters; immutable
}
