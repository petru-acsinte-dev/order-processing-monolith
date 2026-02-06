package spring.orders.demo.users.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table( name = "roles", schema = "users")
public class Role {

	@Id
	private Short id;

	@NotBlank
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

	public void setId(Short id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
