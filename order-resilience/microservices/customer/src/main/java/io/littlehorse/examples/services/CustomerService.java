package io.littlehorse.examples.services;

import java.util.List;

import io.littlehorse.examples.exceptions.CustomerException;
import io.littlehorse.examples.models.Customer;
import io.littlehorse.examples.models.CustomerStatus;
import io.littlehorse.examples.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class CustomerService {
  @Inject
  CustomerRepository repository;

  public List<Customer> getAllCustomers() {
    return repository.listAll();
  }

  public void validateCustomer(Long id) throws CustomerException {
     Customer customer = repository.findById(id);
     if (customer == null) {
         throw new CustomerException("Customer not found with id: " + id);
     }
     if (customer.getStatus() != CustomerStatus.ACTIVE) {
         throw new CustomerException("Customer is not active: " + id);
     }
  }

}
