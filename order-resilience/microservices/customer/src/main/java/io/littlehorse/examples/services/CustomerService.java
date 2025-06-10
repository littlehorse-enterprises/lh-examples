package io.littlehorse.examples.services;

import java.util.List;

import io.littlehorse.examples.exceptions.CustomerDisabledException;
import io.littlehorse.examples.exceptions.CustomerNotFoundException;
import io.littlehorse.examples.models.Customer;
import io.littlehorse.examples.models.CustomerStatus;
import io.littlehorse.examples.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;


@ApplicationScoped
public class CustomerService {
    CustomerRepository repository;

    CustomerService(CustomerRepository customerRepository) {
        this.repository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return repository.listAll();
    }


    @Transactional
    public void validateCustomer(Long id) {
        Customer customer = repository.findById(id);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new CustomerDisabledException("Customer is not active: " + id);
        }
    }

}
