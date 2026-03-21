package spring.orders.demo.ship.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import spring.orders.demo.constants.Constants;
import spring.orders.demo.orders.dto.OrderInfo;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.ship.dto.FulfillmentResponse;
import spring.orders.demo.ship.services.FulfillmentService;

@Tag (name = "Fulfillments controller", description = "Operations related to order fulfillments")
@RestController
@RequestMapping(path = Constants.FULFILLMENTS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class FulfillmentController {

	private final FulfillmentService service;

	public FulfillmentController(FulfillmentService service) {
		this.service = service;
	}

	@GetMapping
	@Operation (summary = "Lists fulfillments",
			description = "Lists available fulfillments, newest first. Requires admin priviledges.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
					array = @ArraySchema(schema = @Schema(implementation = FulfillmentResponse.class))))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges",
			content = @Content(schema = @Schema(hidden = true)))
	public Page<FulfillmentResponse> getFulfillments(Pageable pageable) {
		SecurityUtils.confirmAdminRole();

		return service.getFulfillments(pageable);
	}

	@GetMapping("/{orderId}")
	@Operation (summary = "Returns fulfillment for an order",
			description = "Returns fulfillment information for the specified order. Requires admin priviledges.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
					array = @ArraySchema(schema = @Schema(implementation = FulfillmentResponse.class))))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "The specified order does not have a fulfillment",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<FulfillmentResponse> getFulfillment(
			@PathVariable UUID orderId) {
		SecurityUtils.confirmAdminRole();

		final FulfillmentResponse fulfillment = service.getOrderFulfillment(orderId);

		return ResponseEntity.ok(fulfillment);
	}

	@PostMapping(path = "/{orderId}/ship")
	@Operation (summary = "Marks an order fulfillment as shipped",
			description = "Marks an existing order fulfillment as shipped. Requires admin priviledges.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = OrderInfo.class)))
	@ApiResponse (responseCode = "403",
			description = "The user does not have permissions to mark the order as shipped",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Order not found",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<FulfillmentResponse> shipOrder(@PathVariable UUID orderId) {
		SecurityUtils.confirmAdminRole();

		final FulfillmentResponse fulfillment = service.shipFulfilment(orderId);

		return ResponseEntity.ok(fulfillment);
	}

}
