package io.littlehorse.orderresilience.customer.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
    List<Customer> findByName(String name);
}
