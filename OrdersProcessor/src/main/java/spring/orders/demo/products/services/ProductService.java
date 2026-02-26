package spring.orders.demo.products.services;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import spring.orders.demo.constants.UserRole;
import spring.orders.demo.products.dto.ProductResponse;
import spring.orders.demo.products.mappers.ProductMapper;
import spring.orders.demo.products.repositories.ProductRepository;
import spring.orders.demo.security.SecurityUtils;

@Service
public class ProductService {

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

}
