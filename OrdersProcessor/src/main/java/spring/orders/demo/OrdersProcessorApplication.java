package spring.orders.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrdersProcessorApplication {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		SpringApplication.run(OrdersProcessorApplication.class, args);
	}

}
