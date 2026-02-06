package spring.orders.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;

@Configuration
public class OpenAPIConfiguration {

	private static final String X_USER = "x-USER"; //$NON-NLS-1$

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addParameters(X_USER, new HeaderParameter()
                    .name(X_USER)
                    .description("Username or email of the requestor (admin required for admin endpoints)") //$NON-NLS-1$
                    .required(true)
                    .schema(new io.swagger.v3.oas.models.media.StringSchema()))
            )
            .info(new Info().title("Customer User API").version("v1")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
