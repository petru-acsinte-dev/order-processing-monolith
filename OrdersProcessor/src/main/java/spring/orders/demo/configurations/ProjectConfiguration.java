package spring.orders.demo.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan (basePackages = {"spring.orders.demo.users", "spring.orders.demo.logging"})
public class ProjectConfiguration {

}
