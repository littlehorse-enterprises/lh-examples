package io.littlehorse.orderresilience.customer.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import io.littlehorse.orderresilience.customer.model.Customer;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
}
