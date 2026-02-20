package spring.orders.demo.users.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateCustomerUserRequest extends AbstractCustomerUserDTO {

    @NotBlank
    private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "UpdateCustomerUserRequest [getEmail()=" + getEmail()  //$NON-NLS-1$
				+ ", getAddress()="	+ getAddress() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
