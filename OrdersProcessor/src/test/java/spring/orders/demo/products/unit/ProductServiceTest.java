package spring.orders.demo.products.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.products.dto.CreateProductRequest;
import spring.orders.demo.products.dto.MoneyDTO;
import spring.orders.demo.products.dto.ProductResponse;
import spring.orders.demo.products.entities.Money;
import spring.orders.demo.products.entities.Product;
import spring.orders.demo.products.mappers.ProductMapper;
import spring.orders.demo.products.repositories.ProductRepository;
import spring.orders.demo.products.services.ProductService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	private static final String CAD = "CAD"; //$NON-NLS-1$

	@Mock
	private ProductRepository repository;

	@Mock
	private ProductMapper mapper;

	@InjectMocks
	private ProductService service;

	@BeforeEach
	void setupAuth() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						Constants.ADMIN, Constants.ADMIN, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))); //$NON-NLS-1$
	}

	@AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

	@Test
	void testGetAllProducts() {
		final UUID uuid1 = UUID.randomUUID();
		final UUID uuid2 = UUID.randomUUID();
		final UUID uuid3 = UUID.randomUUID();
		final Product first = new Product("SKU-000001", "LG 34\" UltraWide Monitor", //$NON-NLS-1$//$NON-NLS-2$
				Money.of(BigDecimal.valueOf(399), Currency.getInstance(CAD)));
		first.setActive(true);
		first.setId(1L);
		first.setExternalId(uuid1);
		first.setDescription("34-inch curved IPS monitor"); //$NON-NLS-1$

		final Product second = new Product("SKU-000002", "Sony WH-1000XM5 Headphones", //$NON-NLS-1$//$NON-NLS-2$
				Money.of(BigDecimal.valueOf(349.99), Currency.getInstance(CAD)));
		second.setActive(true);
		second.setId(2L);
		second.setExternalId(uuid2);
		second.setDescription("Noise cancelling wireless headphones"); //$NON-NLS-1$

		final Product third = new Product("SKU-000003", "Anker PowerCore 20000", //$NON-NLS-1$//$NON-NLS-2$
				Money.of(BigDecimal.valueOf(49.99), Currency.getInstance(CAD)));
		third.setActive(true);
		third.setId(3L);
		third.setExternalId(uuid3);
		third.setDescription("20000mAh portable power bank"); //$NON-NLS-1$

		final List<Product> expectedProducts = List.of(first, second, third);

		given(repository.findAll(Pageable.unpaged(Sort.by("name")))) //$NON-NLS-1$
			.willReturn(new PageImpl<>(expectedProducts));

		given(mapper.toResponse(first))
			.willReturn(new ProductResponse(first.getExternalId().toString(),
											first.getSku(),
											first.getName(),
											first.getDescription(),
											true,
											new MoneyDTO(first.getCost().getAmount(), first.getCost().getCurrency())));

		given(mapper.toResponse(second))
			.willReturn(new ProductResponse(second.getExternalId().toString(),
											second.getSku(),
											second.getName(),
											second.getDescription(),
											true,
											new MoneyDTO(second.getCost().getAmount(), second.getCost().getCurrency())));

		given(mapper.toResponse(third))
			.willReturn(new ProductResponse(third.getExternalId().toString(),
											third.getSku(),
											third.getName(),
											third.getDescription(),
											true,
											new MoneyDTO(third.getCost().getAmount(), third.getCost().getCurrency())));

		final List<ProductResponse> products = service.getAllProducts();
		assertNotNull(products);
		assertEquals(3, products.size());
		for (int index = 0; index < 3; index++) {
			final var expected = expectedProducts.get(index);
			final var actual = products.get(index);
			assertEquals(expected.getExternalId().toString(), actual.getExternalId());
			assertEquals(expected.getName(), actual.getName());
			assertEquals(expected.getCost().getAmount(), actual.getCost().getAmount());
			assertEquals(expected.getCost().getCurrency(), actual.getCost().getCurrency());
			assertEquals(expected.getSku(), actual.getSku());
		}
	}

	@Test
	void testCreateProduct() {
		assertDoesNotThrow(this::doCreateProduct);
	}

	@Test
	void testCreateProductAsRegularUser() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(Constants.ADMIN, Constants.ADMIN));

		assertThrows(Exception.class, this::doCreateProduct);
	}

	private void doCreateProduct() {
		final String sku = "SKU-000001"; //$NON-NLS-1$
		final String name = "LG 34\" UltraWide Monitor";  //$NON-NLS-1$
		final String desc = "34-inch curved IPS monitor"; //$NON-NLS-1$
		final MoneyDTO cost = new MoneyDTO(BigDecimal.valueOf(399), Currency.getInstance(CAD));
		final CreateProductRequest productRequest = new CreateProductRequest();
		productRequest.setName(name);
		productRequest.setSku(sku);
		productRequest.setDescription(desc);
		productRequest.setCost(cost);

		final UUID staticUUID = UUID.randomUUID();
		final String expectedExternalId = staticUUID.toString();
		final ProductResponse expectedResponse = new ProductResponse(staticUUID.toString(), sku, name, desc, true, cost);
		try (MockedStatic<UUID> mockedUUID = Mockito.mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(staticUUID);

            final Product saved = mockEntity(productRequest);

            // lenient() allows testing as regular user when an exception is thrown early
            lenient().when(repository.save(saved))
            	.thenReturn(saved);
            lenient().when(mapper.toResponse(saved))
            	.thenReturn(expectedResponse);

            final ProductResponse response = service.createProduct(productRequest);

            verify(repository).save(any(Product.class));

            assertEquals(productRequest.getName(), response.getName());
            assertEquals(productRequest.getCost().getAmount(), response.getCost().getAmount());
            assertEquals(productRequest.getCost().getCurrency(), response.getCost().getCurrency());
            assertEquals(productRequest.getSku(), response.getSku());
            assertEquals(true, response.isActive());
            assertEquals(productRequest.getDescription(), response.getDescription());
            assertEquals(expectedExternalId, response.getExternalId());
		}
	}

	private Product mockEntity(CreateProductRequest request) {
		final Product entity = new Product(request.getSku(), request.getName(), mockCost(request.getCost()));
		if (null != request.getDescription()) {
			entity.setDescription(request.getDescription());
		}
		entity.setActive(true);

		lenient().when(mapper.toEntity(request))
			.thenReturn(entity);

		return entity;
	}

	private Money mockCost(MoneyDTO cost) {
		return Money.of(cost.getAmount(), cost.getCurrency());
	}
}
