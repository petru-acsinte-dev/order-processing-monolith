package spring.orders.demo.ship.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import spring.orders.demo.ship.entities.Fulfillment;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {

	Page<Fulfillment> findAllSortByCreatedDesc(Pageable pagingRequest);

	Optional<Fulfillment> findByOrderExternalId(UUID orderExternalId);

}
