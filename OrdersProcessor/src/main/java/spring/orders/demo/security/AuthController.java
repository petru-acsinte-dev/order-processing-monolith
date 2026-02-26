package spring.orders.demo.security;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import spring.orders.demo.constants.Constants;

@Tag(name = "Authentication controller", description = "Provides a JWT for valid credentials")
@RestController
public class AuthController {

	Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

	private final AuthenticationManager authManager;
	private final JWTService jwtService;

	public AuthController(AuthenticationManager authManager, JWTService jwtService) {
		this.authManager = authManager;
		this.jwtService = jwtService;
	}

	@Operation(summary = "Authentication endpoint", description = "Authenticates the provided credentials and provides a JWT")
	@ApiResponse(responseCode = "200",
				description = "Successful authentication",
				content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthResponse.class)))
	@ApiResponse(responseCode = "401",
				description = "Incorrect credentials",
				content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthError.class)))
	@PostMapping(value = Constants.LOGIN_PATH)
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
		log.debug("Login attempt: username={}, password={}", request.getUsername(), request.getPassword()); //$NON-NLS-1$
		// check the user/password against storage through UserDetailsSecurityService
		final Authentication authentication = authManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		final UserDetails details = (UserDetails) authentication.getPrincipal();
		// generate authenticated token
		final String token = jwtService.generateToken(details);
		return ResponseEntity.ok(new AuthResponse(token));
	}
}
