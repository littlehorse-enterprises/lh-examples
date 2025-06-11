package io.littlehorse.examples.services;

import java.util.List;

import io.littlehorse.examples.exceptions.OrderBlocked;
import io.littlehorse.examples.exceptions.CustomerNotFoundException;
import io.littlehorse.examples.models.Customer;
import io.littlehorse.examples.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
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
        if (!customer.getCanPlaceOrders()) {
            throw new OrderBlocked(String.format("%s is not allowed to place orders",customer.getName()));
        }
    }

}
