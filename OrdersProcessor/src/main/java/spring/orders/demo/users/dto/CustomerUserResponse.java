package spring.orders.demo.users.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public class CustomerUserResponse extends AbstractCustomerUserDTO {

	@Schema(description = "Unique external user identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private String externalId;

	@Schema(example = "johndoe")
	private String username;

	@Schema(description = "User creation date and time")
	private LocalDateTime created;

	@Schema(example = "USER", defaultValue = "USER")
	private String role;

	@Schema(example = "ACTIVE")
	private String status;

	public CustomerUserResponse() {}

	/**
	 * CustomerUser DTO constructor.
	 * @param externalId The external UUID uniquely identifying the user.
	 * @param username The username.
	 * @param email The email.
	 * @param created The time the user was created.
	 * @param role The user's role.
	 * @param status The user's status.
	 * @param address The user's address.
	 */
	public CustomerUserResponse(String externalId, String username, String email,
			LocalDateTime created, String role,
			String status, AddressDTO address) {
		super(email, address);
		this.externalId = externalId;
		this.username = username;
		this.created = created;
		this.role = role;
		this.status = status;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "CustomerUserResponse [externalId=" + externalId  //$NON-NLS-1$
				+ ", username=" + username  //$NON-NLS-1$
				+ ", created=" + created //$NON-NLS-1$
				+ ", role=" + role  //$NON-NLS-1$
				+ ", status=" + status  //$NON-NLS-1$
				+ ", getEmail()=" + getEmail()  //$NON-NLS-1$
				+ ", getAddress()="	+ getAddress() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}