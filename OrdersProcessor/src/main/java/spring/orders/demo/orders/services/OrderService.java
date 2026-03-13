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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.orders.OrderProps;
import spring.orders.demo.orders.dto.CreateOrderRequest;
import spring.orders.demo.orders.dto.OrderInfo;
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
import spring.orders.demo.orders.exceptions.OrderCannotBeModifiedException;
import spring.orders.demo.orders.exceptions.OrderNotFoundException;
import spring.orders.demo.orders.exceptions.ProductNotFoundException;
import spring.orders.demo.orders.exceptions.TooManyProductsInRequest;
import spring.orders.demo.orders.mappers.OrderMapper;
import spring.orders.demo.orders.repositories.OrderRepository;
import spring.orders.demo.orders.repositories.ProductRepository;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.users.entities.CustomerUser;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;
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

	/**
	 * Creates a new order with the specified product(s) and quantities.
	 * @param createRequest The create request DTO.
	 * @return The new order response DTO.
	 * @throws EmptyProductsListException if no products are present in the request
	 * @throws IncompatibleProductCurrencies if the added products have different currencies (known limitation)
	 * @throws ProductNotFoundException if at least one specified product cannot be found
	 * @throws TooManyProductsInRequest if the request size exceeds system limits
	 *
	 */
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

	/**
	 * Updates an existing order with the specified product(s) and quantities.
	 * The update request specifies a collection of line items to add or update and a collection of product external identifiers to remove.
	 * If the same product identifier is present in both collections, deletion takes precedence.
	 * @param updatecreateRequest The update request DTO.
	 * @return The updated order response DTO.
	 * @throws IncompatibleProductCurrencies if the added products have different currencies than the order (known limitation)
	 * @throws ProductNotFoundException if at least one specified product cannot be found
	 * @throws TooManyProductsInRequest if the request size exceeds system limits
	 */
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

	/**
	 * Returns the order for the specified external Id.
	 * @param externalId The order unique external identifier.
	 * @return {@link OrderResponse} DTO with order details.
	 * @throws OrderNotFoundException if the order cannot be found.
	 */
	@Transactional(readOnly = true)
	public OrderResponse getOrder(UUID externalId) {
		final Order order = orderRepository.findByExternalIdWithLinesAndProducts(externalId)
			.orElseThrow(OrderNotFoundException::new);

		checkAuthorization(order);

		return mapper.orderToOrderResponse(order);
	}

	/**
	 * Returns the orders in paged responses. By default the newest orders are first.
	 * @param ownerId The orders owner external identifier. If null, it means all accessible orders.
	 * @param pageable Pagination and sorting information.
	 * @return A page containing {@link OrderInfo} DTOs with order basic details.
	 * @throws UnauthorizedOperationException if the user making the request does not have access to the orders of the specified owner.
	 */
	@Transactional(readOnly = true)
	public Page<OrderInfo> getOrders(@RequestParam(required = false) UUID ownerId,
			Pageable pageable) {

		UUID requestOwnerId = ownerId;
		final UUID currentUserExternalId = getUserExternalId();
		if (null != requestOwnerId) {
			if ( ! currentUserExternalId.equals(requestOwnerId)) {
				// only an admin can see the orders for another user
				SecurityUtils.confirmAdminRole();
			}
		} else if ( ! SecurityUtils.hasRole(Constants.ADMIN_ROLE)) {
			// defaults to current user is no filter mentioned and not an admin
			requestOwnerId = currentUserExternalId;
		}

		final Pageable pageRequest = getPagingRequest(pageable);

		final boolean filterbyOwner = null != requestOwnerId;
		log.debug("Finding orders for {}", filterbyOwner ? requestOwnerId : "all");   //$NON-NLS-1$//$NON-NLS-2$
		if (filterbyOwner) {
			return orderRepository
					.findAllByCustomerExternalIdOrderByCreatedDesc(requestOwnerId, pageRequest)
					.map(mapper::toInfo);
		}
		return orderRepository.findAllByOrderByCreatedDesc(pageRequest)
			.map(mapper::toInfo);
	}

	private void checkAuthorization(Order order) {
		if ( ! getUserExternalId().equals(order.getCustomerExternalId())) {
			SecurityUtils.confirmAdminRole();
		}
	}

	private void checkImmutability(Order order) {
		final String orderStatus = order.getStatus().getStatus();
		if ( ! spring.orders.demo.constants.OrderStatus.CREATED.equals(orderStatus)) {
			throw new OrderCannotBeModifiedException(orderStatus);
		}
	}

	private OrderResponse updateOrder(UUID orderExternalId,
			Map<UUID, Integer> productsToUpsert, List<Product> products,
			List<UUID> externalIdsToRemove) {
		final Order order = orderRepository.findByExternalIdWithLinesAndProducts(orderExternalId)
									.orElseThrow(OrderNotFoundException::new);

		checkAuthorization(order);

		checkImmutability(order);

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

		final Currency orderCurrency = order.getCost().getCurrency();
		for (final Product newProduct : products) {
			if (toRemove.contains(newProduct.getExternalId())) {
				// remove takes precedence
				continue;
			}
			if ( ! orderCurrency.getCurrencyCode().equals(newProduct.getCost().getCurrency().getCurrencyCode())) {
				throw new IncompatibleProductCurrencies(orderCurrency, newProduct.getCost().getCurrency());
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

	private Pageable getPagingRequest(Pageable pageable) {
		int pageNo = 0;
		int pageSize = orderProps.getPageSize();
		Sort sortBy = null == orderProps.getDefaultSortAttribute() ?
				null : Sort.by(orderProps.getDefaultSortAttribute());
		if (null != pageable) {
			if (pageable.getPageNumber() > 0) {
				pageNo = pageable.getPageNumber();
			}
			final int requestSize = pageable.getPageSize();
			if (requestSize > 0
			&& requestSize <= Constants.PAGE_SIZE_HARD_LIMIT // system imposed limit
			&& requestSize <= orderProps.getMaxPageSize()) {
				pageSize = requestSize;
			}
			if (null == sortBy) {
				sortBy = pageable.getSort();
			} else {
				sortBy = pageable.getSortOr(sortBy);
			}
		}
		if (null == sortBy) {
			return PageRequest.of(pageNo, pageSize);
		}
		return PageRequest.of(pageNo, pageSize, sortBy);
	}

}
