package io.littlehorse.examples.workflows;

import io.littlehorse.examples.tasks.OrderTask;
import io.littlehorse.quarkus.workflow.LHWorkflow;
import io.littlehorse.quarkus.workflow.LHWorkflowDefinition;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

import java.lang.annotation.Annotation;
@LHWorkflow(OrderWorkflow.ORDER_WORKFLOW)
public class OrderWorkflow implements LHWorkflowDefinition {
    public static final String ORDER_WORKFLOW = "order-workflow";
    public static final String ORDER_VARIABLE = "order";
    public static final String REDUCE_STOCK = "reduce-stock";
    public static final String VALIDATE_CUSTOMER = "validate-customer";


    @Override
    public void define(WorkflowThread wf) {
        WfRunVariable order = wf.declareJsonObj(ORDER_VARIABLE).required();
        WfRunVariable shouldExit = wf.declareBool("exit").withDefault(false);
        WfRunVariable orderId = wf.declareInt("order-id").withDefault(-1);
        NodeOutput orderNode= wf.execute(OrderTask.SAVE_ORDER_TASK, order);
        NodeOutput customerNode = wf.execute(VALIDATE_CUSTOMER,order.jsonPath("$.clientId"));
//        wf.mutate(orderId, VariableMutationType.ASSIGN,1);
        orderId.assign(orderNode.jsonPath("$.orderId"));
        wf.handleException(customerNode,"order-blocked", handler->{
            WfRunVariable content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            handler.execute(OrderTask.UPDATE_ORDER_STATUS,1,"CANCELED",content);
            shouldExit.assign(true);
        });
        wf.doIf(shouldExit.isEqualTo(true), WorkflowThread::complete);
        NodeOutput productNode= wf.execute(REDUCE_STOCK,order.jsonPath("$.orderLines"));
        wf.handleException(productNode,"out-of-stock",handler->{
            WfRunVariable content= handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            handler.execute(OrderTask.UPDATE_ORDER_STATUS,orderId,"CANCELED",content);
            shouldExit.assign(true);
        });
        wf.doIf(shouldExit.isEqualTo(true), WorkflowThread::complete);
        wf.execute(OrderTask.UPDATE_ORDER_STATUS,orderId, "COMPLETED","Order completed successfully");
    }
}
