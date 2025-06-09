package io.littlehorse.orderresilience.customer.tasks;

import io.littlehorse.orderresilience.customer.services.CustomerService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;

@LHTask
public class CustomerTask {
    public static final String VALIDATE_CUSTOMER = "validate-customer";

    CustomerService customerService;

    public CustomerTask(CustomerService customerService) {
        this.customerService = customerService;
    }

    @LHTaskMethod(VALIDATE_CUSTOMER)
    public void validateCustomer(Integer customerId) throws Exception {
        this.customerService.validateCustomer(customerId);
    }
}
