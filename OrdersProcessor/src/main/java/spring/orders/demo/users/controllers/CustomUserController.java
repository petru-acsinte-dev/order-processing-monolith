package spring.orders.demo.users.controllers;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
import spring.orders.demo.XUserHeader;
import spring.orders.demo.users.dto.CreateCustomerUserRequest;
import spring.orders.demo.users.dto.CustomerUserResponse;
import spring.orders.demo.users.dto.UpdateCustomerUserRequest;
import spring.orders.demo.users.services.CustomerUserService;

@Tag (name = "Users controller", description = "Operations related to users management")
@RestController
@RequestMapping("/users")
@XUserHeader
public class CustomUserController {

	private final CustomerUserService service;

	public CustomUserController(CustomerUserService service) {
		this.service = service;
	}

	@GetMapping
	@Operation (summary = "Lists all users",
			description = "Lists all users present in the system. Requires admin priviledges.")
	@ApiResponse (responseCode = "200",
				description = "Users retrieved successfully",
				content = @Content(mediaType = "application/json",
								array = @ArraySchema(schema = @Schema(implementation = CustomerUserResponse.class))))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges")
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request")
	public List<CustomerUserResponse> findAll(@RequestHeader (name = "x-USER") String requestorIdentifier) {
		return service.findAllUsers(requestorIdentifier);
	}

	@PostMapping
	@Operation (summary = "Creates a new user",
				description = "Creates a new user. Requires admin priviledges.")
	@ApiResponse (responseCode = "201",
				description = "User created successfully",
				content = @Content(mediaType = "application/json",
									array = @ArraySchema(schema = @Schema(implementation = CustomerUserResponse.class))))
	@ApiResponse (responseCode = "403",
				description = "User does not have the required priviledges")
	@ApiResponse (responseCode = "401",
				description = "Unauthorized user request")
	public ResponseEntity<CustomerUserResponse> createUser(
			@RequestHeader (name = "x-USER") String requestorIdentifier,
			@RequestBody CreateCustomerUserRequest createRequest) {
		final CustomerUserResponse newUser = service.createUser(requestorIdentifier, createRequest);
		return ResponseEntity
			.created(URI.create("/users/" + newUser.getExternalId())) //$NON-NLS-1$
			.body(newUser);
	}

	@PutMapping
	@Operation(summary = "Updates an existing user",
			description = "Updates the email and/or address for an existing user. Requires admin priviledges.")
	@ApiResponse (responseCode = "200",
			description = "User updated successfully",
			content = @Content(mediaType = "application/json",
						array = @ArraySchema(schema = @Schema(implementation = CustomerUserResponse.class))))
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges")
	@ApiResponse (responseCode = "401",
			description = "Unauthorized user request")
	@Parameter(name = "externalId", required = true)
	public ResponseEntity<CustomerUserResponse> updateUser(
			@RequestHeader (name = "x-USER") String requestorIdentifier,
			@RequestParam (required = true) String externalId,
			@RequestBody UpdateCustomerUserRequest updateRequest) {
		final UUID external = UUID.fromString(externalId);
		final CustomerUserResponse updatedUser = service.updateUser(requestorIdentifier, external, updateRequest);
		return ResponseEntity
			.ok(updatedUser);
	}

	@DeleteMapping
	@Operation(summary = "Archives an existing user",
			description = "Archives an existing user. Requires admin priviledges.")
	@ApiResponse (responseCode = "204",
			description = "User deleted successfully")
	@ApiResponse (responseCode = "403",
			description = "User does not have the required priviledges")
	@ApiResponse (responseCode = "401",
			description = "Unauthorized user request")
	@Parameter(name = "externalId", required = true)
	public ResponseEntity<CustomerUserResponse> updateUser(
			@RequestHeader (name = "x-USER") String requestorIdentifier,
			@RequestParam (required = true) String externalId) {
		final UUID external = UUID.fromString(externalId);
		service.deleteUser(requestorIdentifier, external);
		return ResponseEntity
			.noContent()
			.build();
	}
}
