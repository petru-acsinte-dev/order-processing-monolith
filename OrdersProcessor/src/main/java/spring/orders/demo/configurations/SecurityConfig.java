package spring.orders.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.filters.JWTFilter;
import spring.orders.demo.filters.JsonLoginFilter;
import spring.orders.demo.security.JWTService;
import spring.orders.demo.security.UserDetailsSecurityService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final UserDetailsSecurityService userDetails;
    private final JWTService jwtService;
    private final ObjectMapper objectMapper;

    public SecurityConfig(UserDetailsSecurityService userDetails,
                          JWTService jwtService,
                          ObjectMapper objectMapper) {
        this.userDetails = userDetails;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Bean
    JWTFilter jwtFilter(JWTService jwtService, UserDetailsSecurityService userDetailsService) {
        return new JWTFilter(jwtService, userDetailsService);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager) throws Exception {

        // Define the filter beans
    	final JWTFilter jwtFilter = jwtFilter(jwtService, userDetails);
        final JsonLoginFilter jsonLoginFilter = new JsonLoginFilter(authManager, jwtService, objectMapper);

        http.csrf(CsrfConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(Constants.LOGIN_PATH,
                    				Constants.ACTUATOR_HEALTH,
                    				Constants.ACTUATOR_INFO,
                    				Constants.SWAGGER_UI,
                    				Constants.V3_API_DOCS
                                ).permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            // JWT filter protects all other endpoints
            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            // Add JSON login filter before JWT filter to avoid empty body
            .addFilterBefore(jsonLoginFilter, JWTFilter.class);

        return http.build();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetails);
        provider.setPasswordEncoder(new BCryptPasswordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}