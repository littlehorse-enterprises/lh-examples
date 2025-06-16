package io.littlehorse.examples.workflows;

import io.littlehorse.examples.services.CouponService;
import io.littlehorse.examples.tasks.CouponTasks;
import io.littlehorse.quarkus.workflow.LHWorkflow;
import io.littlehorse.quarkus.workflow.LHWorkflowDefinition;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import org.apache.kafka.common.protocol.types.Field;

@LHWorkflow(CouponWorkflow.WORKFLOW_NAME)
public class CouponWorkflow implements LHWorkflowDefinition {
    public static final String WORKFLOW_NAME = "create-coupon-workflow";
    public static final String CLIENT_ID = "client-id";
    public static final String PRODUCT_ID = "product-id";
    public static final String PRODUCT_NAME = "product-name";


    @Override
    public void define(WorkflowThread wf) {
        var clientId = wf.declareInt(CLIENT_ID).required();
        var productId = wf.declareInt(PRODUCT_ID).required();
        var productName = wf.declareStr(PRODUCT_NAME).required();
        wf.execute(CouponTasks.CREATE_COUPON, clientId, productId, productName);
    }
}
