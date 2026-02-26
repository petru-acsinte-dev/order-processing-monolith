package spring.orders.demo.products.entities;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "currencies", schema = "orders")
public class Currency {

	@Id
	private Short id;

	@NotNull
	@Column(length = 3, columnDefinition = "CHAR(3)")
	private String currency;

	@Override
	public int hashCode() {
		return Objects.hash(currency, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final Currency other = (Currency) obj;
		return Objects.equals(currency, other.currency);
	}

	public Short getId() {
		return id;
	}

	public void setId(Short id) {
		this.id = id;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
