package spring.orders.demo.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

	@NotBlank
	@Schema (example = "johndoe")
	private String username;

	@NotBlank
	private String password;

	public AuthRequest() {}

	@JsonCreator
    public AuthRequest(@JsonProperty("username") String username,
                       @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

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

}
