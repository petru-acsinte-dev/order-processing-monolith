package spring.orders.demo.users.controllers;

import java.net.URI;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import spring.orders.demo.constants.Constants;
import spring.orders.demo.response.PagedResponse;
import spring.orders.demo.response.ResponseUtils;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.services.CustomerUserService;

@Tag (name = "Users controller", description = "Operations related to users management")
@RestController
@RequestMapping(path = Constants.USERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomUserController {

	private final CustomerUserService service;

	public CustomUserController(CustomerUserService service) {
		this.service = service;
	}

	@GetMapping
	@Operation (summary = "Lists users",
			description = "Lists users present in the system. Requires admin priviledges.")
	@ApiResponse (responseCode = "200",
				description = "Users retrieved successfully",
				headers = @Header(
			            name = "Link",
			            description = "Pagination links with rel=next and rel=prev",
			            required = false))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request",
				content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<PagedResponse<CustomerUserResponse>> getUsers(@ParameterObject
																		@Parameter(required = false)
																		Pageable pageable) {
		final Page<CustomerUserResponse> page = service.findUsers(pageable);

		return ResponseUtils.getPagedResponse(page);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation (summary = "Creates a new user",
				description = "Creates a new user. Requires admin priviledges.")
	@ApiResponse (responseCode = "201",
				description = "User created successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
									array = @ArraySchema(schema = @Schema(implementation = CustomerUserResponse.class))))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request",
				content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<CustomerUserResponse> createUser(
			@Valid @RequestBody CreateCustomerUserRequest createRequest) {
		final CustomerUserResponse newUser = service.createUser(createRequest);
		return ResponseEntity
			.created(URI.create("/users/" + newUser.getExternalId())) //$NON-NLS-1$
			.body(newUser);
	}

	@PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@Operation(summary = "Updates an existing user",
			description = "Updates the email and/or address for an existing user. Requires admin priviledges.")
	@ApiResponse (responseCode = "200",
			description = "User updated successfully",
			content = @Content(mediaType = "application/json",
						array = @ArraySchema(schema = @Schema(implementation = CustomerUserResponse.class))))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request",
				content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<CustomerUserResponse> updateUser(
			@RequestParam (required = true) String externalId,
			@Valid @RequestBody UpdateCustomerUserRequest updateRequest) {
		final UUID external = UUID.fromString(externalId);
		final CustomerUserResponse updatedUser = service.updateUser(external, updateRequest);
		return ResponseEntity
			.ok(updatedUser);
	}

	@DeleteMapping(produces = {})
	@Parameter(name = Constants.PARAM_EXTERNAL_ID, required = true)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Archives an existing user",
				description = "Archives an existing user. Requires admin priviledges.")
	@ApiResponse (responseCode = "204",
				description = "User deleted successfully",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges",
				content = @Content(schema = @Schema(hidden = true)))
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request",
				content = @Content(schema = @Schema(hidden = true)))
	public ResponseEntity<CustomerUserResponse> deleteUser(
			@RequestParam (required = true) String externalId) {
		final UUID external = UUID.fromString(externalId);
		service.deleteUser(external);
		return ResponseEntity
			.noContent()
			.build();
	}

}
