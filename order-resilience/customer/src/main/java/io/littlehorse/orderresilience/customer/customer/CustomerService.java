package io.littlehorse.orderresilience.customer.customer;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
  @Autowired
  private CustomerRepository repository;

  public Customer get(UUID id) {
    return repository.getById(id);
  }

  public Customer create(Customer customer) {
    return repository.save(customer);
  }

}
