package spring.orders.demo.orders.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import spring.orders.demo.orders.dto.CreateProductRequest;
import spring.orders.demo.orders.dto.ProductResponse;
import spring.orders.demo.orders.dto.UpdateProductRequest;
import spring.orders.demo.orders.services.ProductService;

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
	public Page<ProductResponse> getAvailableProducts(Pageable pageable) {
		return service.getProducts(pageable);
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
		description = "Product deleted successfully",
		content = @Content(schema = @Schema(hidden = true)))
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
	@DeleteMapping
	public ResponseEntity<ProductResponse> deleteProduct(@RequestParam(required = true) @NotBlank String externalId) {
		final UUID uuid = UUID.fromString(externalId);
		service.deleteProduct(uuid);
		return ResponseEntity
				.noContent()
				.build();
	}

	@Operation (summary = "Creates a product",
			description = "Admin operation which creates a product in the system.")
	@ApiResponse(responseCode = "201",
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
	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest createRequest) {
		final ProductResponse newProduct = service.createProduct(createRequest);
		return ResponseEntity
				.created(URI.create(String.format("%s/%s",  //$NON-NLS-1$
						Constants.PRODUCTS_PATH, newProduct.getExternalId())))
				.body(newProduct);
	}
}
