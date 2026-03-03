package spring.orders.demo.products.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import spring.orders.demo.constants.Constants;
import spring.orders.demo.products.dto.CreateProductRequest;
import spring.orders.demo.products.dto.ProductResponse;
import spring.orders.demo.products.dto.UpdateProductRequest;
import spring.orders.demo.products.services.ProductService;

@Tag (name = "Products controller", description = "Operations related to products management")
@RestController
@RequestMapping(Constants.PRODUCTS_PATH)
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@Operation (summary = "Lists products",
			description = "Lists products present in the system.")
	@ApiResponse(responseCode = "200",
				content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
						array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request",
				content = @Content(schema = @Schema(hidden = true)))
	@GetMapping
	public List<ProductResponse> getAvailableProducts() {
		return service.getAllProducts();
	}

	@Operation (summary = "Updates a product",
				description = "Admin operation which updates a product present in the system.")
	@ApiResponse(responseCode = "200",
	content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = ProductResponse.class)))
	@ApiResponse (responseCode = "403",
		description = "User does not have the required priviledges",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
		description = "Unauthorized request",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
		description = "Product not found",
		content = @Content(schema = @Schema(hidden = true)))
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@PatchMapping
	public ProductResponse updateProduct(@RequestParam(required = true) @NotBlank String externalId,
				@Valid @RequestBody UpdateProductRequest updateRequest) {
		final UUID uuid = UUID.fromString(externalId);
		return service.updateProduct(uuid, updateRequest);
	}

	@Operation (summary = "Deletes a product",
			description = "Admin operation which archives a product present in the system.")
	@ApiResponse(responseCode = "204",
	content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = ProductResponse.class)))
	@ApiResponse (responseCode = "403",
		description = "User does not have the required priviledges",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
		description = "Unauthorized request",
		content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
		description = "Product not found",
		content = @Content(schema = @Schema(hidden = true)))
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@PatchMapping
	public ProductResponse deleteProduct(@RequestParam(required = true) @NotBlank String externalId) {
		final UUID uuid = UUID.fromString(externalId);
		return service.deleteProduct(uuid);
	}

	@Operation (summary = "Creates a product",
			description = "Admin operation which creates a product in the system.")
	@ApiResponse(responseCode = "200",
	content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
		schema = @Schema(implementation = ProductResponse.class)))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
			description = "Unauthorized request",
			content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "404",
			description = "Product not found",
			content = @Content(schema = @Schema(hidden = true)))
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@PatchMapping
	public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest createRequest) {
		return service.createProduct(createRequest);
	}
}
