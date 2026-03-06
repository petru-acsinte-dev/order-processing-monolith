package spring.orders.demo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;

public final class SecurityUtils {

	private static Logger log = LoggerFactory.getLogger(SecurityUtils.class);

	// this class should not be instantiated
	private SecurityUtils() {}

	/**
	 * Obtains the username associated with the current security context.
	 * @return The username
	 */
	public static String getUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	/**
	 * Checks if the user has the specified role
	 * @param role The role name (e.g. ROLE_USER, ROLE_ADMIN)
	 * @return True if the role is associated with the user's security context
	 */
	public static boolean hasRole(String role) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

	/**
	 * Checks if the user is an admin. If not throws {@link UnauthorizedOperationException}
	 */
	public static void confirmAdminRole() {
		if (SecurityUtils.hasRole(Constants.ADMIN_ROLE)) {
			return;
		}
		log.warn("Forbidden: User {} is not an admin user", SecurityUtils.getUsername()); //$NON-NLS-1$
		throw new UnauthorizedOperationException();
	}

}
