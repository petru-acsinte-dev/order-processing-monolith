package spring.orders.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import spring.orders.demo.Constants;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addParameters(Constants.X_USER, new HeaderParameter()
                    .name(Constants.X_USER)
                    .description("Username or email of the requestor (admin required for admin endpoints)") //$NON-NLS-1$
                    .required(true)
                    .schema(new io.swagger.v3.oas.models.media.StringSchema()))
            )
            .info(new Info().title("Customer User API").version("v1")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
