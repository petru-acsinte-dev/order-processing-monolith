package spring.orders.demo.users.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table( name = "roles", schema = "users")
public class Role {

	@Id
	// role ids are predefined; not generated
	private Short id;

	@NotNull
	@Column (nullable = false, unique = true)
	private String role;

	protected Role() {}

	public Role(Short id, String role) {
		this.id = id;
		this.role = role;
	}

	public Short getId() {
		return id;
	}

	public String getRole() {
		return role;
	}

	// no setters; immutable
}
