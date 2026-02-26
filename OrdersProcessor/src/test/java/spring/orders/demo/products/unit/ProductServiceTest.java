package spring.orders.demo.products.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import spring.orders.demo.constants.Constants;
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
		final List<Product> expectedProducts = List.of(
				new Product(1L, uuid1, "SKU-000001", "LG 34\" UltraWide Monitor", "34-inch curved IPS monitor", true,   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						Money.of(BigDecimal.valueOf(399), Currency.getInstance(CAD))),
			new Product(2L, uuid2, "SKU-000002", "Sony WH-1000XM5 Headphones", "Noise cancelling wireless headphones", true,   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					Money.of(BigDecimal.valueOf(349.99), Currency.getInstance(CAD))),
			new Product(3L, uuid3, "SKU-000003", "Anker PowerCore 20000", "20000mAh portable power bank", true,   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					Money.of(BigDecimal.valueOf(49.99), Currency.getInstance(CAD)))
			);
		given(repository.findAll(Pageable.unpaged(Sort.by("name")))) //$NON-NLS-1$
			.willReturn(new PageImpl<>(expectedProducts));

		final var first = expectedProducts.get(0);
		given(mapper.toResponse(first))
			.willReturn(new ProductResponse(first.getExternalId().toString(),
											first.getSku(),
											first.getName(),
											first.getDescription(),
											true,
											new MoneyDTO(first.getCost().getAmount(), first.getCost().getCurrency())));

		final var second = expectedProducts.get(1);
		given(mapper.toResponse(second))
			.willReturn(new ProductResponse(second.getExternalId().toString(),
											second.getSku(),
											second.getName(),
											second.getDescription(),
											true,
											new MoneyDTO(second.getCost().getAmount(), second.getCost().getCurrency())));

		final var third = expectedProducts.get(2);
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
}
