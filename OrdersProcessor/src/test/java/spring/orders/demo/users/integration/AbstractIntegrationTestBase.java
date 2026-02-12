package spring.orders.demo.users.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@Testcontainers
@SpringBootTest
class AbstractIntegrationTestBase {

	@Autowired
	Flyway flyWay;

	@SuppressWarnings("resource")
	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15") //$NON-NLS-1$
														.withDatabaseName("postgresdb") //$NON-NLS-1$
														.withUsername("posttest") //$NON-NLS-1$
														.withPassword("postPass") //$NON-NLS-1$
														.withReuse(true)
														.withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl); //$NON-NLS-1$
		registry.add("spring.datasource.username", postgres::getUsername); //$NON-NLS-1$
		registry.add("spring.datasource.password", postgres::getPassword); //$NON-NLS-1$
	}

	@BeforeEach
	void migrateDatabase() {
		flyWay.migrate();
	}

	@AfterEach
	void cleanDatabase() {
		final JdbcTemplate jdbc = new JdbcTemplate(
	            new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
	        );
			jdbc.execute("DROP SCHEMA ship CASCADE"); //$NON-NLS-1$
			jdbc.execute("DROP SCHEMA orders CASCADE"); //$NON-NLS-1$
			jdbc.execute("DROP SCHEMA users CASCADE"); //$NON-NLS-1$
			jdbc.execute("DROP TABLE public.flyway_schema_history"); //$NON-NLS-1$
	}

}
