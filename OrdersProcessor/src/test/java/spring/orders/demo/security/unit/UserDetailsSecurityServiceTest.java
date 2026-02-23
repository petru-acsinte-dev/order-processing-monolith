package spring.orders.demo.security.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static spring.orders.demo.users.unit.CustomerUserServiceTest.getAdminUser;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import spring.orders.demo.security.UserDetailsSecurityService;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.repositories.CustomerUserRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserDetailsSecurityServiceTest {

	@Mock
	private CustomerUserRepository repository;

	@InjectMocks
	private UserDetailsSecurityService detailsService;

	@Test
	@DisplayName("Loads the admin user and checks the role")
	void testLoadAdminUser() {
		final CustomerUser admin = getAdminUser();

		given(repository.findByUsername(admin.getUsername()))
			.willReturn(Optional.of(admin));

		final UserDetails details = detailsService.loadUserByUsername(admin.getUsername());

		assertThat(details.getUsername()).isEqualTo(admin.getUsername());
		assertThat(details.getAuthorities())
			.extracting(GrantedAuthority::getAuthority)
			.containsExactly("ROLE_ADMIN"); //$NON-NLS-1$
	}

	@Test
	@DisplayName("Confirms an exception is thrown if no matching user is found")
	void testNoUser() {
		final CustomerUser admin = getAdminUser();
		final var username = admin.getUsername();

		given(repository.findByUsername(username))
			.willReturn(Optional.empty());

		assertThatThrownBy(()->detailsService.loadUserByUsername(username))
			.isInstanceOf(UsernameNotFoundException.class);

	}
}
