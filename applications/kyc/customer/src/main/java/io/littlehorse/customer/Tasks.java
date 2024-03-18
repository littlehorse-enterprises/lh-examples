package io.littlehorse.customer;

import io.littlehorse.customer.customer.Customer;
import io.littlehorse.customer.customer.CustomerService;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Tasks {
  CustomerService customerService;

  public Tasks(CustomerService customerService) {
    this.customerService = customerService;
  }

  @LHTaskMethod("create-customer")
  public Customer shipItem(Customer customer) throws Exception {
    try {
      log.info(String.format("Creating customer: %s", customer.getEmail()));
      return this.customerService.create(customer);
    } catch (Exception e) {
      // Business exception
      log.error(e.getMessage());
      throw new LHTaskException("user-exists", "email already exists");
    }
    // Technical exceptions aren't handled
  }
}
