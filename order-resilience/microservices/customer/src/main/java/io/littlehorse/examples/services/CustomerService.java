package io.littlehorse.examples.services;

import java.util.List;

import io.littlehorse.examples.exceptions.OrderBlocked;
import io.littlehorse.examples.exceptions.CustomerNotFoundException;
import io.littlehorse.examples.models.Customer;
import io.littlehorse.examples.repository.CustomerRepository;
import io.littlehorse.sdk.common.proto.VariableValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;


@ApplicationScoped
public class CustomerService {
    @Inject
    CustomerRepository repository;

    public List<Customer> getAllCustomers() {
        return repository.listAll();
    }

    @Transactional
    public void validateCustomer(Long id) {
        Customer customer = repository.findById(id);
        if (customer == null) {
            String errorMessage = "Customer not found with id: " + id;
            throw new CustomerNotFoundException(errorMessage);
        }
        if (!customer.getCanPlaceOrders()) {
            String errorMessage = String.format("%s is not allowed to place orders", customer.getName());
            throw new OrderBlocked(errorMessage);
        }
    }

}
