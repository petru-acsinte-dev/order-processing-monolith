package spring.orders.demo.shared;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import spring.orders.demo.constants.Constants;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public abstract class AbstractUnitTestBase {

	@BeforeEach
	void setupAuth() {
		setupAdmin();
	}

	@AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

	protected void setupAdmin() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						Constants.ADMIN, Constants.ADMIN, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))); //$NON-NLS-1$
	}

	protected void setupUserNoRole(String username, String password) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(username, password)); // no role
	}
}
