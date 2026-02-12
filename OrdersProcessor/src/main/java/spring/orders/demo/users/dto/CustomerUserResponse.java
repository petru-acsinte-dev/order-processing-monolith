package spring.orders.demo.users.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public class CustomerUserResponse {

	@Schema(description = "Unique external user identifier",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private String externalId;

	@Schema(example = "johndoe")
	private String username;

	@Schema(example = "johnd@order.processor.com")
	private String email;

	@Schema(description = "User creation date and time")
	private LocalDateTime created;

	@Schema(example = "USER", defaultValue = "USER")
	private String role;

	@Schema(example = "ACTIVE")
	private String status;

	private AddressDTO address;

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
		this.externalId = externalId;
		this.username = username;
		this.email = email;
		this.created = created;
		this.role = role;
		this.status = status;
		this.address = address;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "CustomerUserResponse [externalId=" + externalId  //$NON-NLS-1$
				+ ", username=" + username  //$NON-NLS-1$
				+ ", email=" + email //$NON-NLS-1$
				+ ", created=" + created  //$NON-NLS-1$
				+ ", role=" + role  //$NON-NLS-1$
				+ ", status=" + status  //$NON-NLS-1$
				+ ", address=" + address + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}