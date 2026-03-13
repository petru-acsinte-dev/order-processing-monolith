package spring.orders.demo.orders.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import spring.orders.demo.constants.Constants;
import spring.orders.demo.orders.dto.CreateOrderRequest;
import spring.orders.demo.orders.dto.OrderResponse;
import spring.orders.demo.orders.dto.UpdateOrderRequest;
import spring.orders.demo.orders.services.OrderService;

@RestController
@RequestMapping(Constants.ORDERS_PATH)
public class OrderController {

	private final OrderService service;

	public OrderController(OrderService service) {
		this.service = service;
	}

	@Operation (summary = "Creates an order",
			description = "Creates an order in the system containing the submitted products and quantities.")
	@ApiResponse(responseCode = "201",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponse (responseCode = "400",
			description = "Bad, incomplete, or request is too big",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Product not found",
			content = @Content(schema = @Schema(hidden = true)))
	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createRequest) {
		final OrderResponse newOrder = service.createOrder(createRequest);
		return ResponseEntity
				.created(URI.create(String.format("%s/%s",  //$NON-NLS-1$
						Constants.ORDERS_PATH, newOrder.getExternalId())))
				.body(newOrder);
	}

	@Operation (summary = "Updates an order",
			description = "Adds, removes and changes product quantities in an existing order.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponse (responseCode = "400",
			description = "Bad, incomplete, or request is too big",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	@PatchMapping("/{orderId}")
	public ResponseEntity<OrderResponse> updateOrder(
			@PathVariable UUID orderId,
			@Valid @RequestBody UpdateOrderRequest updateRequest) {
		final OrderResponse changedOrder = service.updateOrder(orderId, updateRequest);
		return ResponseEntity
				.ok(changedOrder);
	}

}
