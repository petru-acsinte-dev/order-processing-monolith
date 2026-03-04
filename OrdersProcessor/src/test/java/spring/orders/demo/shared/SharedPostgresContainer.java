package spring.orders.demo.shared;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SharedPostgresContainer {
	@SuppressWarnings("resource")
	public static final PostgreSQLContainer<?> POSTGRES =
	        new PostgreSQLContainer<>("postgres:15") //$NON-NLS-1$
	            .withDatabaseName("postgresdb") //$NON-NLS-1$
	            .withUsername("posttest") //$NON-NLS-1$
	            .withPassword("postPass"); //$NON-NLS-1$

	    static {
	        POSTGRES.start();

	        Flyway.configure()
            .dataSource(POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword())
            .load()
            .migrate();

	        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	            if (POSTGRES.isRunning()) {
	                POSTGRES.stop();
	                POSTGRES.close();
	            }
	        }));
	    }
}
