package spring.orders.demo.ship.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import spring.orders.demo.ship.entities.ShipStatus;

@Repository
public interface ShipStatusRepository extends JpaRepository<ShipStatus, Short> {

	Optional<ShipStatus> findByStatus(String status);

}
