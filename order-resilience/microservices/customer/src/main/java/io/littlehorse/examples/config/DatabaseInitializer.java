package io.littlehorse.examples.config;

import io.littlehorse.examples.repository.CustomerRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class);

    @Inject
    CustomerRepository customerRepository;

    @Transactional
    public void onStart(@Observes StartupEvent event) {
        LOG.info("Initializing database connections on application startup");
        // Execute a simple query to ensure database connections are established
        long customerCount = customerRepository.count();
        LOG.info("Database initialized: Found " + customerCount + " customers");
    }
}
