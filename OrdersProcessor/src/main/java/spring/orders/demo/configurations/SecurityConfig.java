package spring.orders.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {
		return http
				.csrf(CsrfConfigurer::disable)
				// FIXME: fix this
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // temporary allow ALL
				.build();
	}

}
