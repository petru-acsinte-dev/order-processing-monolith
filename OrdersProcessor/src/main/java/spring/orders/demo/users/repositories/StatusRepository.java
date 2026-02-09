package spring.orders.demo.users.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import spring.orders.demo.users.entities.Status;

public interface StatusRepository extends JpaRepository<Status, Long> {

	Optional<Status> findByStatus(String status);

}
