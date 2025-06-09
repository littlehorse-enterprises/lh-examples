package io.littlehorse.orderresilience.customer.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.littlehorse.orderresilience.customer.customer.exceptions.CustomerException;


@Service
public class CustomerService {
  @Autowired
  private CustomerRepository repository;

  public List<Customer> getAllCustomers() {
    List<Customer> customers = new ArrayList<>();
    repository.findAll().forEach(customers::add);
    return customers;
  }

  public void validateCustomer(Integer id) throws CustomerException {
     Customer customer = repository.findById(id)
            .orElseThrow(() -> new CustomerException("Customer not found with id: " + id));
     if (customer.getStatus() != CustomerStatus.ACTIVE) {
         throw new CustomerException("Customer is not active: " + id);
     }
  }

}
