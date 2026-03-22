package spring.orders.demo.ship.controllers;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import spring.orders.demo.constants.Constants;
import spring.orders.demo.response.PagedResponse;
import spring.orders.demo.response.ResponseUtils;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.ship.dto.ShipmentResponse;
import spring.orders.demo.ship.services.ShipmentService;

@Tag (name = "Shipments controller", description = "Operations related to order shipments")
@RestController
@RequestMapping(path = Constants.SHIPMENTS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ShipmentController {

	private final ShipmentService service;

	public ShipmentController(ShipmentService service) {
		this.service = service;
	}

	@GetMapping
	@Operation (summary = "Lists shipments",
			description = "Lists available shipments, newest first. Requires admin priviledges.")
	@ApiResponse(responseCode = "200",
			headers = @Header(
		            name = "Link",
		            description = "Pagination links with rel=next and rel=prev",
		            required = false))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<PagedResponse<ShipmentResponse>> getFulfillments(	@ParameterObject
																			@Parameter(required = false)
																			Pageable pageable) {
		SecurityUtils.confirmAdminRole();

		final var page = service.getShipments(pageable);

		return ResponseUtils.getPagedResponse(page);
	}

	@GetMapping("/{orderId}")
	@Operation (summary = "Returns shipment for an order",
			description = "Returns shipment information for the specified order. Requires admin priviledges.")
	@ApiResponse(responseCode = "200",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
					array = @ArraySchema(schema = @Schema(implementation = ShipmentResponse.class))))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "The specified order does not have a fulfillment",
			content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<ShipmentResponse> getFulfillment(
			@PathVariable UUID orderId) {
		SecurityUtils.confirmAdminRole();

		final ShipmentResponse fulfillment = service.getOrderShipment(orderId);

		return ResponseEntity.ok(fulfillment);
	}

}
