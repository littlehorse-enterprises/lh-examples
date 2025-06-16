package io.littlehorse.examples.workflows;

import io.littlehorse.examples.tasks.OrderTask;
import io.littlehorse.quarkus.workflow.LHWorkflow;
import io.littlehorse.quarkus.workflow.LHWorkflowDefinition;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

import java.lang.annotation.Annotation;

@LHWorkflow(OrderWorkflow.ORDER_WORKFLOW)
public class OrderWorkflow implements LHWorkflowDefinition {
    public static final String ORDER_WORKFLOW = "order-workflow";
    public static final String ORDER_VARIABLE = "order";
    public static final String REDUCE_STOCK = "dispatch-order";
    public static final String VALIDATE_CUSTOMER = "validate-customer";


    @Override
    public void define(WorkflowThread wf) {
        WfRunVariable order = wf.declareJsonObj(ORDER_VARIABLE).required();
        WfRunVariable shouldExit = wf.declareStr("exit").withDefault("");
        WfRunVariable orderId = wf.declareInt("order-id").withDefault(-1);
        WfRunVariable clientId = wf.declareInt("client-id").withDefault(-1);
        NodeOutput orderResponse = wf.execute(OrderTask.SAVE_ORDER_TASK, order);
        orderId.assign(orderResponse.jsonPath("$.orderId"));
        clientId.assign(orderResponse.jsonPath("$.clientId"));
        NodeOutput customerResponse = wf.execute(VALIDATE_CUSTOMER, order.jsonPath("$.clientId"));
        wf.handleException(customerResponse, handler -> {
            WfRunVariable content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            NodeOutput orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("customer-validation-failure", "Something happened validating the customer.");
        });
        NodeOutput productNode = wf.execute(REDUCE_STOCK, clientId, order.jsonPath("$.orderLines"));
        wf.handleException(productNode, handler -> {
            WfRunVariable content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            NodeOutput orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("customer-failure", "Failed to dispatch the order");
        });
        NodeOutput finalOutput = wf.execute(OrderTask.FINALIZE_ORDER_TASK, orderId, "COMPLETED", "Your order has been completed and successfully dispatched!");
        wf.throwEvent(ORDER_WORKFLOW, finalOutput);
    }
}
