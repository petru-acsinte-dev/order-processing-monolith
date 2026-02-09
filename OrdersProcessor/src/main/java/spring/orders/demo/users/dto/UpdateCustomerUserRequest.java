package spring.orders.demo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateCustomerUserRequest {

    @NotBlank
    @Email
    @Schema(example = "johndoe@order.processing.com")
    private String email;

    @Valid
    private AddressDTO address;

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

	@Override
	public String toString() {
		return "UpdateCustomerUserRequest [email="  //$NON-NLS-1$
				+ email + ", address="  //$NON-NLS-1$
				+ address + "]"; //$NON-NLS-1$
	}

}
