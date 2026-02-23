package spring.orders.demo.security.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import spring.orders.demo.security.AuthController;
import spring.orders.demo.security.AuthRequest;
import spring.orders.demo.security.AuthResponse;
import spring.orders.demo.security.JWTService;
import spring.orders.demo.users.entities.UserRole;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthenticationManager authManager;

	@Mock
	private JWTService jwtService;

	@InjectMocks
	private AuthController controller;

	@Test
	@DisplayName("Checks that a valid token is returned for proper credentials")
	void testTokenReturned() {
		final String fakeToken = "qwertyuiopasdfghjklzx.fake.token"; //$NON-NLS-1$

		final UserDetails details = User.builder()
			.username("ADMIN") //$NON-NLS-1$
			.password("something") //$NON-NLS-1$
			.roles(UserRole.ADMIN)
			.build();

		given(authManager.authenticate(any()))
			.willReturn(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

		given(jwtService.generateToken(details))
			.willReturn(fakeToken);

		final ResponseEntity<AuthResponse> response = controller.login(new AuthRequest("admin", "password"));  //$NON-NLS-1$//$NON-NLS-2$

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertNotNull(response.getBody());
		assertThat(response.getBody().token()).isEqualTo(fakeToken);
	}

}
