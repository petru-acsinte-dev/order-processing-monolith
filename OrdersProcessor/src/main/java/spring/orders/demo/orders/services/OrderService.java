package spring.orders.demo.orders.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import spring.orders.demo.orders.OrderProps;
import spring.orders.demo.orders.dto.CreateOrderRequest;
import spring.orders.demo.orders.dto.OrderLineRequest;
import spring.orders.demo.orders.dto.OrderResponse;
import spring.orders.demo.orders.dto.UpdateOrderRequest;
import spring.orders.demo.orders.entities.Money;
import spring.orders.demo.orders.entities.Order;
import spring.orders.demo.orders.entities.OrderLine;
import spring.orders.demo.orders.entities.OrderStatus;
import spring.orders.demo.orders.entities.Product;
import spring.orders.demo.orders.exceptions.EmptyProductsListException;
import spring.orders.demo.orders.exceptions.IncompatibleProductCurrencies;
import spring.orders.demo.orders.exceptions.OrderNotFoundException;
import spring.orders.demo.orders.exceptions.ProductNotFoundException;
import spring.orders.demo.orders.exceptions.TooManyProductsInRequest;
import spring.orders.demo.orders.mappers.OrderMapper;
import spring.orders.demo.orders.repositories.OrderRepository;
import spring.orders.demo.orders.repositories.ProductRepository;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.exceptions.UserNotFoundException;
import spring.orders.demo.users.repositories.CustomerUserRepository;

@Service
public class OrderService {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);

	private final CustomerUserRepository userRepository;

	private final OrderRepository orderRepository;

	private final ProductRepository productRepository;

	private final OrderMapper mapper;

	private final OrderProps orderProps;

	public OrderService(CustomerUserRepository userRepository, OrderRepository orderRepository,
			ProductRepository productRepository, OrderMapper mapper, OrderProps orderProps) {
		this.userRepository = userRepository;
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.mapper = mapper;
		this.orderProps = orderProps;
	}

	@Transactional
	public OrderResponse createOrder(CreateOrderRequest createRequest) {
		final Map<UUID, Integer> orderProducts = getProducts(createRequest);

		log.debug("Identifying products to add"); //$NON-NLS-1$
		// note: this might need to be paged if the possibility of having very large orders is real
		final List<Product> products = findAllProducts(orderProducts.keySet(), orderProps.getQueryBatchSize());
		if (orderProducts.size() != products.size()) {
			throw new ProductNotFoundException("Not all the ordered products could be found");
		}
		log.debug("Creating new order for {} products", products.size()); //$NON-NLS-1$
		final Order newOrder = createOrder(orderProducts, products);
		return mapper.orderToOrderResponse(newOrder);
	}

	@Transactional
	public OrderResponse updateOrder(UUID orderExternalId, UpdateOrderRequest updateRequest) {
		final Map<UUID, Integer> productsToUpsert = getProductsToUpsert(updateRequest);
		List<Product> products = Collections.emptyList();
		if (!productsToUpsert.isEmpty()) {
			products = findAllProducts(productsToUpsert.keySet(), orderProps.getQueryBatchSize());
			if (productsToUpsert.size() != products.size()) {
				throw new ProductNotFoundException("Not all the ordered products could be found");
			}
		}

		final List<UUID> externalIdsToRemove = updateRequest.getRemovedProducts();

		return updateOrder(orderExternalId, productsToUpsert, products, externalIdsToRemove);
	}

	private OrderResponse updateOrder(UUID orderExternalId,
			Map<UUID, Integer> productsToUpsert, List<Product> products,
			List<UUID> externalIdsToRemove) {
		final Order order = orderRepository.findByExternalIdWithLinesAndProducts(orderExternalId)
									.orElseThrow(OrderNotFoundException::new);
		final Set<UUID> toRemove = null == externalIdsToRemove ?
				Collections.emptySet() : new HashSet<>(externalIdsToRemove);
		final List<OrderLine> lines = order.getOrderLines();
		lines.removeIf(line -> {
			final UUID productExternalId = line.getProduct().getExternalId();
			if (toRemove.contains(productExternalId)) {
				return true;
			}
			// existing lines only get updated
			final Integer quantity = productsToUpsert.get(productExternalId);
			if (null != quantity) {
				line.setQuantity(quantity);
				productsToUpsert.remove(productExternalId);
			}
			return false;
		});

		for (final Product newProduct : products) {
			if (toRemove.contains(newProduct.getExternalId())) {
				// remove takes precedence
				continue;
			}
			if (productsToUpsert.containsKey(newProduct.getExternalId())) {
				final OrderLine newLine = new OrderLine();
				newLine.setProduct(newProduct);
				newLine.setCost(newProduct.getCost());
				newLine.setQuantity(productsToUpsert.get(newProduct.getExternalId()));
				newLine.setProductName(newProduct.getName());
				newLine.setOrder(order);
				order.getOrderLines().add(newLine);
			}
		}

		return mapper.orderToOrderResponse(order);
	}

	private List<Product> findAllProducts(Collection<UUID> externalIds, int batchSize) {
		if (batchSize <= 0) {
			throw new IllegalArgumentException(String.valueOf(batchSize));
		}
        final List<Product> result = new ArrayList<>();
        final List<UUID> idsList = new ArrayList<>(externalIds);

        for (int i = 0; i < idsList.size(); i += batchSize) {
            final int end = Math.min(i + batchSize, idsList.size());
            final List<UUID> batch = idsList.subList(i, end);
            result.addAll(productRepository.findAllByExternalIdIn(batch));
        }

        return result;
	}

	private Order createOrder(Map<UUID, Integer> orderProducts, List<Product> products) {
		final Order order = new Order();
		order.setCreated(LocalDateTime.now());
		order.setCustomerExternalId(getUserExternalId());
		order.setExternalId(UUID.randomUUID());
		order.setStatus(new OrderStatus(spring.orders.demo.constants.OrderStatus.CREATED_ID,
										spring.orders.demo.constants.OrderStatus.CREATED));
		order.setOrderLines(new ArrayList<>());
		for (final Product product : products) {
			final OrderLine line = new OrderLine();
			line.setProduct(product);
			line.setQuantity(orderProducts.get(product.getExternalId()));
			line.setCost(product.getCost());
			line.setOrder(order);
			// first product dictates the order currency
			if (null != order.getCost()) {
				// impose same currency for all products
				final Currency orderCurrency = order.getCost().getCurrency();
				final Currency productCurrency = product.getCost().getCurrency();
				if ( ! orderCurrency.equals(productCurrency)) {
					throw new IncompatibleProductCurrencies(orderCurrency, productCurrency);
				}
				final Money currentCost = order.getCost();
				final BigDecimal currentTotal = currentCost.getAmount();
				order.setCost(Money.of(currentTotal.add(line.getLineTotal()), orderCurrency));
			} else {
				order.setCost(Money.of(line.getLineTotal(), line.getCost().getCurrency()));
			}

			order.getOrderLines().add(line);
		}
		orderRepository.save(order);
		return order;
	}

	private UUID getUserExternalId() {
		final CustomerUser user = userRepository.findByUsername(SecurityUtils.getUsername())
				.orElseThrow(UserNotFoundException::new);
		return user.getExternalId();
	}

	private Map<UUID, Integer> getProducts(CreateOrderRequest createRequest) {
		final List<OrderLineRequest> products = createRequest.getProducts();
		if (products.isEmpty()) {
			throw new EmptyProductsListException();
		}
		if (products.size() > orderProps.getQueryMaxSize()) {
			log.error("System limit {} exceed by the {} order creation request",  //$NON-NLS-1$
					orderProps.getQueryMaxSize(),
					products.size());
			throw new TooManyProductsInRequest(orderProps.getQueryMaxSize(), products.size());
		}
		return products.stream()
			.collect(Collectors.toMap(OrderLineRequest::getProductId, OrderLineRequest::getQuantity));
	}

	private Map<UUID, Integer> getProductsToUpsert(UpdateOrderRequest updateRequest) {
		final List<OrderLineRequest> products = updateRequest.getUpsertProducts();
		if (null == products || products.isEmpty()) {
			return Collections.emptyMap();
		}
		if (products.size() > orderProps.getQueryMaxSize()) {
			log.error("System limit {} exceed by the {} order creation request",  //$NON-NLS-1$
					orderProps.getQueryMaxSize(),
					products.size());
			throw new TooManyProductsInRequest(orderProps.getQueryMaxSize(), products.size());
		}
		return products.stream()
			.collect(Collectors.toMap(OrderLineRequest::getProductId, OrderLineRequest::getQuantity));
	}
}
