package spring.orders.demo.products.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import spring.orders.demo.constants.UserRole;
import spring.orders.demo.products.dto.CreateProductRequest;
import spring.orders.demo.products.dto.ProductResponse;
import spring.orders.demo.products.dto.UpdateProductRequest;
import spring.orders.demo.products.entities.Product;
import spring.orders.demo.products.exceptions.ProductNotFoundException;
import spring.orders.demo.products.mappers.ProductMapper;
import spring.orders.demo.products.repositories.ProductRepository;
import spring.orders.demo.security.SecurityUtils;

@Service
public class ProductService {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository repository;

	private final ProductMapper mapper;

	public ProductService(ProductRepository repository, ProductMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}


	/**
	 * Retrieves all the products from the database.
	 * If the user has admin role, the inactive products are included as well.
	 * @return A collection of {@link ProductResponse} representing the products.
	 */
	@Transactional(readOnly = true)
	public List<ProductResponse> getAllProducts() {

		final boolean filterInactive = ! SecurityUtils.hasRole(UserRole.ADMIN);
		// FIXME: Unpaged for now
		return repository.findAll(Pageable.unpaged(Sort.by("name"))) //$NON-NLS-1$
			.stream()
			.filter(product -> (!filterInactive || product.isActive()))
			.map(mapper::toResponse)
			.toList();
	}

	/**
	 * Creates a new product. Requires admin role.
	 * @param createRequest The new product {@link CreateProductRequest} details.
	 * @return A {@link ProductResponse} representing the new product state.
	 */
	@Transactional
	public ProductResponse createProduct(CreateProductRequest createRequest) {
		SecurityUtils.confirmAdminRole();

		final Product newProduct = mapper.toEntity(createRequest);
		newProduct.setActive(true);
		newProduct.setExternalId(UUID.randomUUID());
		final Product saved = repository.save(newProduct);
		return mapper.toResponse(saved);
	}

	/**
	 * Updates an existing product. Requires admin role.
	 * @param externalId Product unique identifier.
	 * @param updateRequest The product {@link UpdateProductRequest} changes.
	 * @return A {@link ProductResponse} representing the product updated state.
	 */
	@Transactional
	public ProductResponse updateProduct(UUID externalId, UpdateProductRequest updateRequest) {
		SecurityUtils.confirmAdminRole();

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

	/**
	 * Archives an existing product. Requires admin role.
	 * @param externalId Product unique identifier.
	 * @return A {@link ProductResponse} representing the product updated state.
	 */
	@Transactional
	public ProductResponse deleteProduct(UUID externalId) {
		SecurityUtils.confirmAdminRole();

		log.debug("Finding product identified by {}", externalId); //$NON-NLS-1$
		final Product product = repository.findByExternalId(externalId)
				.orElseThrow(ProductNotFoundException::new);
		product.setActive(false);
		return mapper.toResponse(product);
	}

}
