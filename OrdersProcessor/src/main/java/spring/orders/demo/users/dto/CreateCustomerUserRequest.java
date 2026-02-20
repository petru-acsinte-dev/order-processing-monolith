package spring.orders.demo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class CreateCustomerUserRequest extends AbstractCustomerUserDTO {

    @NotBlank
    @Schema (example = "johndoe")
    private String username;

    @NotBlank
    private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
				+ ", password=" + password  //$NON-NLS-1$
				+ ", getEmail()=" + getEmail()  //$NON-NLS-1$
				+ ", getAddress()=" + getAddress() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}


}
