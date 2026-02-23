package spring.orders.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenAPIConfiguration {

    private static final String BEARER_AUTH = "bearerAuth"; //$NON-NLS-1$

	@Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
            		.addSecuritySchemes(BEARER_AUTH,
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer") //$NON-NLS-1$
                                    .bearerFormat("JWT") //$NON-NLS-1$
                                    .description("Paste your JWT token here"))) //$NON-NLS-1$
            		.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
            .info(new Info().title("Customer User API").version("v2")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
