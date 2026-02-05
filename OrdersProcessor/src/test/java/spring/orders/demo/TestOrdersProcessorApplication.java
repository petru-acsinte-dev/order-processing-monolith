package spring.orders.demo;

import org.springframework.boot.SpringApplication;

public class TestOrdersProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrdersProcessorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
