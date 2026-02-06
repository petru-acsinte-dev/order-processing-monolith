package spring.orders.demo.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

	private static final String REQUEST_ID = "requestId"; //$NON-NLS-1$

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
            final String requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(REQUEST_ID, requestId);

            response.setHeader("X-Request-Id", requestId); //$NON-NLS-1$

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
        }
	}

}
