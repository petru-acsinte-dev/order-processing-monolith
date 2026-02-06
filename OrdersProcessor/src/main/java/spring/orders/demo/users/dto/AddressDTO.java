package spring.orders.demo.users.dto;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class AddressDTO {

	@NotBlank
	private String addressLine1;

	private String addressLine2;

	public AddressDTO(@NotBlank String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public AddressDTO(@NotBlank String addressLine1, String addressLine2) {
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Override
	public String toString() {
		return "AddressDTO [addressLine1=" + addressLine1  //$NON-NLS-1$
				+ ", addressLine2=" + addressLine2 + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
