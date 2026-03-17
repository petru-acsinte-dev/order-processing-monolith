package spring.orders.demo.ship.events.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import spring.orders.demo.ship.events.OrderFulfilledEvent;
import spring.orders.demo.ship.services.ShipmentService;

@Component
public class ShipmentListener {

	Logger log = LoggerFactory.getLogger(ShipmentListener.class);

	private final ShipmentService service;

	public ShipmentListener(ShipmentService service) {
		this.service = service;
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(OrderFulfilledEvent event) {
		try {
			service.createShipment(event.orderExternalId());
		} catch (final RuntimeException e) {
			log.error(e.getMessage(), e);
		}
	}
}
