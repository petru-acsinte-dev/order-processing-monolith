package spring.orders.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	// this class should not be instantiated
	private SecurityUtils() {}

	public static String getUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public static boolean hasRole(String role) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
