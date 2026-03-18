package spring.orders.demo.ship.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import spring.orders.demo.constants.Constants;
import spring.orders.demo.constants.ship.Status;
import spring.orders.demo.orders.ShipProps;
import spring.orders.demo.security.SecurityUtils;
import spring.orders.demo.ship.dto.FulfillmentResponse;
import spring.orders.demo.ship.entities.Fulfillment;
import spring.orders.demo.ship.entities.ShipStatus;
import spring.orders.demo.ship.events.OrderFulfilledEvent;
import spring.orders.demo.ship.exceptions.BadExistingFulfillmentStatusException;
import spring.orders.demo.ship.exceptions.FulfillmentNotFoundException;
import spring.orders.demo.ship.exceptions.ShippingStatusNotFoundException;
import spring.orders.demo.ship.mappers.FulfillmentMapper;
import spring.orders.demo.ship.repositories.FulfillmentRepository;
import spring.orders.demo.ship.repositories.ShipStatusRepository;
import spring.orders.demo.users.exceptions.UnauthorizedOperationException;

@Service
public class FulfillmentService {

	private final FulfillmentRepository repository;

	private final ShipStatusRepository statusRepository;

	private final FulfillmentMapper mapper;

	private final ShipProps shipProps;

	private final ApplicationEventPublisher publisher;

	public FulfillmentService(FulfillmentRepository repository, ShipStatusRepository statusRepository,
			FulfillmentMapper mapper, ShipProps shipProps, ApplicationEventPublisher publisher) {
		this.repository = repository;
		this.statusRepository = statusRepository;
		this.mapper = mapper;
		this.shipProps = shipProps;
		this.publisher = publisher;
	}

	/**
	 * Returns the fulfillments in paged responses. By default the newest fulfillments are first.
	 * @param pageable Pagination and sorting information.
	 * @return A page containing {@link FulfillmentResponse} DTOs with fulfillment basic details.
	 * @throws UnauthorizedOperationException if the user making the request does not have access to the fulfillments.
	 */
	@Transactional(readOnly = true)
	public Page<FulfillmentResponse> getFulfillments(Pageable pageable) {
		SecurityUtils.confirmAdminRole();

		final Pageable pagingRequest = getPagingRequest(pageable);

		final Page<Fulfillment> page = repository.findAll(pagingRequest);

		return page.map(mapper::toResponse);
	}

	/**
	 * Creates a fulfillment record for an order that has been confirmed.
	 * @param orderExternalId The unique order external id.
	 * @return The fulfillment details.
	 */
	@Transactional
	public FulfillmentResponse createFulfilment(UUID orderExternalId) {
		final Optional<Fulfillment> fulfillment = repository.findByOrderExternalId(orderExternalId);

		if (fulfillment.isPresent()) {
			// if a fulfillment exists, it must be ready to ship (in which case this is a no-op)
			final ShipStatus existingStatus = fulfillment.get().getStatus();
			if ( ! Status.READY_TO_SHIP.name().equals(existingStatus.getStatus())) {
				throw new BadExistingFulfillmentStatusException(orderExternalId, existingStatus.getStatus());
			}
			return mapper.toResponse(fulfillment.get());
		}

		final ShipStatus status = statusRepository.findByStatus(Status.READY_TO_SHIP.name())
				.orElseThrow(ShippingStatusNotFoundException::new);

		final Fulfillment newFulfillment = new Fulfillment();
		newFulfillment.setExternalId(UUID.randomUUID());
		newFulfillment.setOrderExternalId(orderExternalId);
		newFulfillment.setStatus(status);

		final Fulfillment created = repository.save(newFulfillment);

		return mapper.toResponse(created);
	}

	/**
	 * Marks a fulfillment record for an order as shipped.
	 * @param orderExternalId The unique order external id.
	 * @return The fulfillment details.
	 */
	@Transactional
	public FulfillmentResponse shipFulfilment(UUID orderExternalId) {
		final Fulfillment fulfillment = repository.findByOrderExternalId(orderExternalId)
				.orElseThrow(() -> new FulfillmentNotFoundException(orderExternalId));

		final ShipStatus status = fulfillment.getStatus();
		if (!Status.READY_TO_SHIP.name().equals(status.getStatus())) {
			throw new BadExistingFulfillmentStatusException(orderExternalId, status.getStatus());
		}

		final ShipStatus shipped = statusRepository.findByStatus(Status.SHIPPED.name())
				.orElseThrow(ShippingStatusNotFoundException::new);

		fulfillment.setStatus(shipped);

		publisher.publishEvent(new OrderFulfilledEvent(orderExternalId));

		return mapper.toResponse(fulfillment);

	}

	/**
	 * Returns the fulfillment for a specific order.
	 * @param orderExternalId Order unique external identifier.
	 * @return A page containing {@link FulfillmentResponse} DTOs with fulfillment basic details.
	 * @throws UnauthorizedOperationException if the user making the request does not have access to the fulfillments.
	 */
	@Transactional(readOnly = true)
	public FulfillmentResponse getOrderFulfillment(@RequestParam(required = true) UUID orderExternalId) {

		SecurityUtils.confirmAdminRole();

		final Fulfillment fulfillment = repository.findByOrderExternalId(orderExternalId)
			.orElseThrow(() -> new FulfillmentNotFoundException(orderExternalId));

		return mapper.toResponse(fulfillment);
	}

	private Pageable getPagingRequest(Pageable pageable) {
		int pageNo = 0;
		int pageSize = shipProps.getPageSize();
		final String sort = shipProps.getFulfillmentSortingAttribute();
		Sort sortBy = null;
		if (null != sort) {
			final String[] split = sort.split(","); //$NON-NLS-1$
			if (split.length > 1) {
				sortBy = Sort.by(split[0]).descending();
			} else {
				sortBy = Sort.by(split[0]);
			}
		}

		if (null != pageable) {
			if (pageable.getPageNumber() > 0) {
				pageNo = pageable.getPageNumber();
			}
			final int requestSize = pageable.getPageSize();
			if (requestSize > 0
			&& requestSize <= Constants.PAGE_SIZE_HARD_LIMIT // system imposed limit
			&& requestSize <= shipProps.getMaxPageSize()) {
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
