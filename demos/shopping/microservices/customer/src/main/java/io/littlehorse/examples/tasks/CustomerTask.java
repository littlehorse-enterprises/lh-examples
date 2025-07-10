package io.littlehorse.examples.tasks;


import io.littlehorse.examples.services.CustomerService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.Date;

@LHTask
public class CustomerTask {
    public static final String VALIDATE_CUSTOMER = "validate-customer";
    private static final Logger LOG = Logger.getLogger(CustomerTask.class);

    CustomerService customerService;

    public CustomerTask(CustomerService customerService) {
        this.customerService = customerService;
    }

    @LHTaskMethod(VALIDATE_CUSTOMER)
    public void validateCustomer(Long customerId, WorkerContext workerContext) throws LHTaskException {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Validating customer %d at %s ", customerId, startTime);
        this.customerService.validateCustomer(customerId);
        var endTime = new Date();
        LOG.infof("Validated customer %d at %s , took %d ms", customerId, endTime, endTime.getTime() - startTime.getTime());
    }
}
