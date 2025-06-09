package io.littlehorse.orderresilience.customer;

import io.littlehorse.orderresilience.customer.service.CustomerService;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Tasks {
  CustomerService customerService;

  public Tasks(CustomerService customerService) {
    this.customerService = customerService;
  }

  @LHTaskMethod("validate-customer")
  public void validateCustomer(Integer customerId) throws Exception {
    try {
      log.info(String.format("Validating customer: %s", customerId));
       this.customerService.validateCustomer(customerId);
    } catch (Exception e) {
      // Business exception
      log.error(e.getMessage());
      throw new LHTaskException("user-exists", "email already exists");
    }
    // Technical exceptions aren't handled
  }
}
