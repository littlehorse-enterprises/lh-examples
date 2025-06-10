package io.littlehorse.examples.tasks;


import io.littlehorse.examples.services.CustomerService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.transaction.Transactional;

@LHTask
public class CustomerTask {
    public static final String VALIDATE_CUSTOMER = "validate-customer";

    CustomerService customerService;

    public CustomerTask(CustomerService customerService) {
        this.customerService = customerService;
    }

    @LHTaskMethod(VALIDATE_CUSTOMER)
    public void validateCustomer(Long customerId) throws LHTaskException {
        this.customerService.validateCustomer(customerId);
    }
}
