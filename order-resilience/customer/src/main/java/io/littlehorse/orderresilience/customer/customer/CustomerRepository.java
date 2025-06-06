package io.littlehorse.orderresilience.customer.customer;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
  Customer getById(UUID id);
}
