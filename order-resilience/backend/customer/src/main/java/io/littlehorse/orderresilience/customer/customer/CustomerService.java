package io.littlehorse.orderresilience.customer.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.littlehorse.orderresilience.customer.customer.exceptions.CustomerNotFoundException;

@Service
public class CustomerService {
  @Autowired
  private CustomerRepository repository;

  public List<Customer> getAllCustomers() {
    List<Customer> customers = new ArrayList<>();
    repository.findAll().forEach(customers::add);
    return customers;
  }

  public Customer get(UUID id) throws CustomerNotFoundException {
    return repository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
  }

  public Customer create(Customer customer) {
    return repository.save(customer);
  }

  public Customer update(UUID id, Customer customerDetails) throws CustomerNotFoundException {
    Customer customer = get(id);
    customer.setEmail(customerDetails.getEmail());
    return repository.save(customer);
  }

  public void delete(UUID id) throws CustomerNotFoundException {
    Customer customer = get(id);
    repository.delete(customer);
  }
  public List<Customer> findBySearchCriteria(String name){
    return repository.findBySearchCriteria(name) ;
  }
}
