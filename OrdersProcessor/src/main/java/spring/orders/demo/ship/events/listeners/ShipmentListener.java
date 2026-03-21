package spring.orders.demo.ship.events.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.ship.events.OrderFulfilledEvent;
import spring.orders.demo.ship.services.ShipmentService;

@Component
public class ShipmentListener {

	Logger log = LoggerFactory.getLogger(ShipmentListener.class);

	private final ShipmentService service;

	public ShipmentListener(ShipmentService service) {
		this.service = service;
	}

	@EventListener
	@Transactional
	public void handle(OrderFulfilledEvent event) {
		try {
			service.createShipment(event.orderExternalId());
		} catch (final RuntimeException e) {
			log.error(e.getMessage(), e);
		}
	}
}
