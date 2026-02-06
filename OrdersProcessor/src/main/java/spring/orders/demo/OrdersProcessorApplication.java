package spring.orders.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OrdersProcessorApplication {

	public static void main(String[] args) {
		try (ConfigurableApplicationContext run = SpringApplication.run(OrdersProcessorApplication.class, args)) {
			// try-with-resources
		}
	}

}
