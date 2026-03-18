package spring.orders.demo.ship.events.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.ship.events.OrderConfirmedEvent;
import spring.orders.demo.ship.services.FulfillmentService;

@Component
public class FulfillmentListener {

	Logger log = LoggerFactory.getLogger(FulfillmentListener.class);

	private final FulfillmentService service;

	public FulfillmentListener(FulfillmentService service) {
		this.service = service;
	}

	@EventListener
	@Transactional
	public void handle(OrderConfirmedEvent event) {
		try {
			service.createFulfilment(event.orderExternalId());
		} catch (final RuntimeException e) {
			log.error(e.getMessage(), e);
		}
	}
}
