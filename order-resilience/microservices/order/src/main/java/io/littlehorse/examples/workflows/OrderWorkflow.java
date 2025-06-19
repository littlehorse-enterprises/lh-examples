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
    public static final String GET_COUPONS_BY_CODES = "get-coupons";
    public static final String APPLY_DISCOUNTS = "apply-discounts";
    public static final String REDEEM_COUPONS = "redeem-coupons";


    @Override
    public void define(WorkflowThread wf) {
        var order = wf.declareJsonObj(ORDER_VARIABLE).required();
        var shouldExit = wf.declareStr("exit").withDefault("");
        var orderId = wf.declareInt("order-id").withDefault(-1);
        var clientId = wf.declareInt("client-id").withDefault(-1);

        var placedOrder = wf.execute(OrderTask.SAVE_ORDER_TASK, order);
        orderId.assign(placedOrder.jsonPath("$.orderId"));
        clientId.assign(placedOrder.jsonPath("$.clientId"));

        var customerResponse = wf.execute(VALIDATE_CUSTOMER, order.jsonPath("$.clientId"));
        wf.handleException(customerResponse, handler -> {
            var content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            var orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELLED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("customer-validation-failure", "Something happened validating the customer.");
        });

        var coupons = wf.execute(GET_COUPONS_BY_CODES, clientId, order.jsonPath("$.discountCodes"));
        wf.handleException(coupons, handler -> {
            var content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            var orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELLED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("coupon-validation-failure", "Something happened validating the coupons.");
        });

        var productsWithDiscounts = wf.execute(APPLY_DISCOUNTS, clientId, order.jsonPath("$.orderLines"), coupons);
        wf.handleException(productsWithDiscounts, handler -> {
            var content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            var orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELLED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("discount-application-failure", "Something happened applying the discounts.");
        });

        var productNode = wf.execute(REDUCE_STOCK, clientId, order.jsonPath("$.orderLines"));
        wf.handleException(productNode, handler -> {
            var content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            var orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELLED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("customer-failure", "Failed to dispatch the order");
        });

        var redeemedCoupons = wf.execute(REDEEM_COUPONS, clientId, order.jsonPath("$.discountCodes"));
        wf.handleException(redeemedCoupons, handler -> {
            var content = handler.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
            var orderCanceled = handler.execute(OrderTask.UPDATE_ORDER_STATUS, orderId, "CANCELLED", content);
            shouldExit.assign(content);
            handler.throwEvent(ORDER_WORKFLOW, orderCanceled);
        });
        wf.doIf(shouldExit.isNotEqualTo(""), handler -> {
            handler.fail("coupon-redeem-failure", "Something happened redeeming the coupons.");
        });

        var completedOrder = wf.execute(OrderTask.FINALIZE_ORDER_TASK, orderId, productsWithDiscounts);
        wf.throwEvent(ORDER_WORKFLOW, completedOrder);
    }
}
