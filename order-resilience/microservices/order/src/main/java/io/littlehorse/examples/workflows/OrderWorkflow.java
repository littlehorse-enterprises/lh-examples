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
    public static final String REDUCE_STOCK = "reduce-stock";
    public static final String VALIDATE_CUSTOMER = "validate-customer";


    @Override
    public void define(WorkflowThread wf) {
        WfRunVariable order = wf.declareJsonObj(ORDER_VARIABLE);
        NodeOutput orderNode= wf.execute(OrderTask.SAVE_ORDER_TASK, order);
        NodeOutput customerNode = wf.execute(VALIDATE_CUSTOMER,order.jsonPath("$.clientId"));
        wf.handleException(customerNode, handler->{
            WfRunVariable content= handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            handler.execute(OrderTask.UPDATE_ORDER_STATUS,orderNode.jsonPath("$.orderId"),"CANCELED",content);
        });
        NodeOutput productNode= wf.execute(REDUCE_STOCK,order.jsonPath("$.orderLines"));
        wf.handleException(productNode,handler->{
            WfRunVariable content= handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            handler.execute(OrderTask.UPDATE_ORDER_STATUS,orderNode.jsonPath("$.orderId"),"CANCELED",content);
        });
        wf.execute(OrderTask.UPDATE_ORDER_STATUS,orderNode.jsonPath("$.orderId"), "COMPLETED","Order completed succesfully");
    }
}
