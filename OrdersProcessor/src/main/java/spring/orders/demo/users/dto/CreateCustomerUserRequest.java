package spring.orders.demo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateCustomerUserRequest {

    @NotBlank
    @Schema (example = "johndoe")
    private String username;

    @NotBlank
    @Email
    @Schema(example = "johnd@order.processor.com")
    private String email;

    @NotBlank
    private String password;

    @Valid
    private AddressDTO address;

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

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "CreateCustomerUserRequest [username=" + username  //$NON-NLS-1$
				+ ", email=" + email  //$NON-NLS-1$
				+ ", address=" + address + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
