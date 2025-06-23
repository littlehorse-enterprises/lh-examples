package io.littlehorse.examples.config;

import io.littlehorse.examples.repositories.OrderRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 * Database initializer that runs on application startup
 * to establish database connections early.
 */
@ApplicationScoped
public class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class);

    @Inject
    OrderRepository orderRepository;

    @Transactional
    public void onStart(@Observes StartupEvent event) {
        LOG.info("Initializing database connections on application startup");
        // Execute a simple query to ensure database connections are established
        long count = orderRepository.count();
        LOG.info("Database initialized: Found " + count + " orders");
    }
}
