package spring.orders.demo.orders.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import spring.orders.demo.orders.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("""
		    SELECT o
		    FROM Order o
		    JOIN FETCH o.orderLines l
		    JOIN FETCH l.product
		    WHERE o.externalId = :id
		""")
		Optional<Order> findByExternalIdWithLinesAndProducts(@Param("id") UUID id);

}
