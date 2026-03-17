package spring.orders.demo.ship.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import spring.orders.demo.ship.entities.Shipment;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

	Page<Shipment> findAllSortByShippedDesc(Pageable pagingRequest);

	Optional<Shipment> findByOrderExternalId(UUID orderExternalId);

}
