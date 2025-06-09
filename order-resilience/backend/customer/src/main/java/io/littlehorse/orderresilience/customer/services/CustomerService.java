package io.littlehorse.orderresilience.customer.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.littlehorse.orderresilience.customer.exceptions.CustomerException;
import io.littlehorse.orderresilience.customer.models.Customer;
import io.littlehorse.orderresilience.customer.models.CustomerStatus;
import io.littlehorse.orderresilience.customer.repository.CustomerRepository;

@ApplicationScoped
public class CustomerService {
  @Inject
  CustomerRepository repository;

  public List<Customer> getAllCustomers() {
    return repository.listAll();
  }

  public void validateCustomer(Integer id) throws CustomerException {
     Customer customer = repository.findById(id);
     if (customer == null) {
         throw new CustomerException("Customer not found with id: " + id);
     }
     if (customer.getStatus() != CustomerStatus.ACTIVE) {
         throw new CustomerException("Customer is not active: " + id);
     }
  }

}
