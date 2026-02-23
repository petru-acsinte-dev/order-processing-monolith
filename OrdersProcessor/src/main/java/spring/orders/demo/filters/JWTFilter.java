package spring.orders.demo.filters;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spring.orders.demo.Constants;
import spring.orders.demo.security.JWTService;
import spring.orders.demo.security.UserDetailsSecurityService;

@Component
public class JWTFilter extends OncePerRequestFilter {

	private final JWTService jwtService;
	private final UserDetailsSecurityService userDetailsService;

	public JWTFilter(JWTService jwtService, UserDetailsSecurityService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if ((null == authHeader) || authHeader.startsWith(Constants.BEARER)) {
			filterChain.doFilter(request, response);
			return;
		}

		final String token = authHeader.substring(Constants.BEARER.length());

		final String username = jwtService.getUsername(token);

		if ((null != username) && (null == SecurityContextHolder.getContext().getAuthentication())) {
			final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			if (jwtService.isTokenValid(token, userDetails)) {
				final var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}

}
