package spring.orders.demo.orders.events.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.constants.order.Status;
import spring.orders.demo.orders.services.OrderService;
import spring.orders.demo.ship.events.OrderShippedEvent;

@Component
public class OrderListener {
	Logger log = LoggerFactory.getLogger(OrderListener.class);

	private final OrderService service;

	public OrderListener(OrderService service) {
		this.service = service;
	}

	@EventListener
	@Transactional
	public void handle(OrderShippedEvent event) {
		try {
			service.updateOrder(event.orderExternalId(), Status.SHIPPED);
		} catch (final RuntimeException e) {
			log.error(e.getMessage(), e);
		}
	}
}
