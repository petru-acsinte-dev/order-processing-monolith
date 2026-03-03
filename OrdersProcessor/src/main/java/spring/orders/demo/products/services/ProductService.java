package spring.orders.demo.products.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.UserRole;
import spring.orders.demo.products.dto.CreateProductRequest;
import spring.orders.demo.products.dto.ProductResponse;
import spring.orders.demo.products.dto.UpdateProductRequest;
import spring.orders.demo.products.entities.Product;
import spring.orders.demo.products.exceptions.ProductNotFoundException;
import spring.orders.demo.products.mappers.ProductMapper;
import spring.orders.demo.products.repositories.ProductRepository;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;

@Service
public class ProductService {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository repository;

	private final ProductMapper mapper;

	public ProductService(ProductRepository repository, ProductMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public List<ProductResponse> getAllProducts() {

		final boolean filterInactive = ! SecurityUtils.hasRole(UserRole.ADMIN);
		// FIXME: Unpaged for now
		return repository.findAll(Pageable.unpaged(Sort.by("name"))) //$NON-NLS-1$
			.stream()
			.filter(product -> (!filterInactive || product.isActive()))
			.map(mapper::toResponse)
			.toList();
	}

	public ProductResponse createProduct(CreateProductRequest createRequest) {
		checkIfAdmin();

		final Product newProduct = mapper.toEntity(createRequest);
		final Product saved = repository.save(newProduct);
		return mapper.toResponse(saved);
	}

	public ProductResponse updateProduct(UUID externalId, UpdateProductRequest updateRequest) {
		checkIfAdmin();

		log.debug("Finding product identified by {}", externalId); //$NON-NLS-1$
		final Product product = repository.findByExternalId(externalId)
				.orElseThrow(ProductNotFoundException::new);
		// reminder: SKU never changes; create a new product for a different SKU
		if (null != updateRequest.getName()) {
			product.setName(updateRequest.getName());
		}
		if (null != updateRequest.getDescription()) {
			product.setDescription(updateRequest.getDescription());
		}
		if (null != updateRequest.getCost()) {
			product.setCost(mapper.toMoney(updateRequest.getCost()));
		}
		return mapper.toResponse(product);
	}

	public ProductResponse deleteProduct(UUID externalId) {
		checkIfAdmin();

		log.debug("Finding product identified by {}", externalId); //$NON-NLS-1$
		final Product product = repository.findByExternalId(externalId)
				.orElseThrow(ProductNotFoundException::new);
		product.setActive(false);
		return mapper.toResponse(product);
	}

	private void checkIfAdmin() {
		if (SecurityUtils.hasRole(Constants.ADMIN_ROLE)) {
			return;
		}
		log.warn("Forbidden: User {} is not an admin user", SecurityUtils.getUsername()); //$NON-NLS-1$
		throw new UnauthorizedOperationException();
	}
}
