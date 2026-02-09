package spring.orders.demo.filters;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spring.orders.demo.users.exceptions.UserAuthenticationFailure;
import spring.orders.demo.users.services.CustomerUserService;

@Component
public class UserHeaderFilter extends OncePerRequestFilter {

	private static final String X_USER = "x-USER"; //$NON-NLS-1$

	private final CustomerUserService service;

	public UserHeaderFilter(CustomerUserService service) {
		this.service = service;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String xUser = request.getHeader(X_USER);

		if ((null == xUser) || xUser.isBlank()) {
			response.sendError(HttpStatus.FORBIDDEN.value(), String.format("%s header required", X_USER)); //$NON-NLS-1$
			return;
		}

		try {
			service.login(xUser);
		} catch (final UserAuthenticationFailure ex) {
			String error = ex.getMessage();
			if (null == error || error.isBlank()) {
				error = ex.getClass().getCanonicalName();
			}
			response.sendError(HttpStatus.FORBIDDEN.value(), error);
			return;
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		final String path = request.getRequestURI();
		return path.startsWith("/swagger") //$NON-NLS-1$
				|| path.startsWith("/actuator") //$NON-NLS-1$
				|| path.startsWith("/v3"); //$NON-NLS-1$
	}

}
