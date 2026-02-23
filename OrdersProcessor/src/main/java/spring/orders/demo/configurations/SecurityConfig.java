package spring.orders.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import spring.orders.demo.filters.JWTFilter;
import spring.orders.demo.security.UserDetailsSecurityService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String LOGIN_PATH = "/login/auth/**"; //$NON-NLS-1$
	private final UserDetailsSecurityService userDetails;
	private final JWTFilter jwtFilter;

	public SecurityConfig(UserDetailsSecurityService userDetails, JWTFilter jwtFilter) {
		this.userDetails = userDetails;
		this.jwtFilter = jwtFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// not needed for stateless REST APIs using JWT
		http.csrf(CsrfConfigurer::disable)

			.authorizeHttpRequests(auth -> auth.requestMatchers(LOGIN_PATH).permitAll()
					.anyRequest().authenticated())

			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authenticationProvider(authenticationProvider())

			//.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			;

		return http.build();
	}

	@Bean
	AuthenticationManager getAuthenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	private AuthenticationProvider authenticationProvider() {
		final DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetails);
		provider.setPasswordEncoder(new BCryptPasswordEncoder());
		return provider;
	}

}
