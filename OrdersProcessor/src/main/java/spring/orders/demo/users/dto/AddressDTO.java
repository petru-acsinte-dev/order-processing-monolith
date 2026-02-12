package spring.orders.demo.users.dto;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class AddressDTO {

	@NotBlank
	@Schema (example = "3401 Hillview Avenue, Palo Alto, CA 94304, USA")
	private String addressLine1;

	private String addressLine2;

	public AddressDTO() {}

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

	@Override
	public int hashCode() {
		return Objects.hash(addressLine1, addressLine2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final AddressDTO other = (AddressDTO) obj;
		return Objects.equals(addressLine1, other.addressLine1) && Objects.equals(addressLine2, other.addressLine2);
	}

}
