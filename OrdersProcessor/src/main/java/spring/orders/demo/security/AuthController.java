package spring.orders.demo.security;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import spring.orders.demo.Constants;

@RestController
public class AuthController {

	Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

	private final AuthenticationManager authManager;
	private final JWTService jwtService;

	public AuthController(AuthenticationManager authManager, JWTService jwtService) {
		this.authManager = authManager;
		this.jwtService = jwtService;
	}

	@PostMapping(value = Constants.LOGIN_PATH)
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		log.debug("Login attempt: username={}, password={}", request.username(), request.password()); //$NON-NLS-1$
		// check the user/password against storage through UserDetailsSecurityService
		final Authentication authentication = authManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));
		final UserDetails details = (UserDetails) authentication.getPrincipal();
		// generate authenticated token
		final String token = jwtService.generateToken(details);
		return ResponseEntity.ok(new AuthResponse(token));
	}
}
