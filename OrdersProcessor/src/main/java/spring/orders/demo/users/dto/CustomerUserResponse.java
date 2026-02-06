package spring.orders.demo.users.dto;

import java.time.LocalDateTime;

public class CustomerUserResponse {

	private String externalId;

	private String username;

	private String email;

	private LocalDateTime created;

	private String role;

	private String status;

	private AddressDTO address;

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