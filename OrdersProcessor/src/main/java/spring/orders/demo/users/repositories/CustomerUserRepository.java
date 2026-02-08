package spring.orders.demo.users.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import spring.orders.demo.users.entities.CustomerUser;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Long> {

	Optional<CustomerUser> findByExternalId(UUID externalId);

	Optional<CustomerUser> findByUsername(String username);

    Optional<CustomerUser> findByEmail(String email);

}
