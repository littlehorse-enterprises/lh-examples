package io.littlehorse.orderresilience.customer.repository;

import io.littlehorse.orderresilience.customer.models.Customer;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {
    
    public Customer findById(Integer id) {
        return find("id", id).firstResult();
    }
}
