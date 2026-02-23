package spring.orders.demo.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import spring.orders.demo.users.entities.UserStatus;
import spring.orders.demo.users.repositories.CustomerUserRepository;

@Service
public class UserDetailsSecurityService implements org.springframework.security.core.userdetails.UserDetailsService {

	private final CustomerUserRepository repository;

	public UserDetailsSecurityService(CustomerUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
		return repository.findByUsername(identifier)
				.map(user -> User.builder()
						.username(user.getUsername())
						.password(user.getPassword())
						.roles(user.getRole().getRole())
						.disabled( ! UserStatus.ACTIVE.equals(user.getStatus().getStatus()))
						.build())
				.orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", identifier))); //$NON-NLS-1$
	}

}
