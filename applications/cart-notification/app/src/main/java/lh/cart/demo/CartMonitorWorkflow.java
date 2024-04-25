package lh.cart.demo;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class CartMonitorWorkflow {
    public static final String WF_NAME = "cart-management";
    public static final String NOTIFY_CART_TASK = "notify-cart";

    public void defineWorkflow(WorkflowThread wf) {
        // Variable to track if the cart has been checked out
        var isCheckedOut = wf.addVariable("isCheckedOut", false);

        // Input variable to represent product being added to cart
        var products = wf.addVariable("products", VariableType.JSON_ARR).searchable();

        wf.doWhile(
                wf.condition(isCheckedOut, Comparator.EQUALS, false),
                loopBody -> {
                    loopBody.sleepSeconds(60); // Wait for 1 minute
                    loopBody.doIf(
                            wf.condition(cartItemsCount, Comparator.GREATER_THAN, 0),
                            ifBody -> {
                                ifBody.execute(NOTIFY_CART_TASK, productId);
                            }
                    );
                }
        );

        // Register an interrupt handler to modify the cart items
        wf.registerInterruptHandler("add-to-cart", handler -> {
             WfRunVariable product = handler.addVariable(WorkflowThread.HANDLER_INPUT_VAR, VariableType.JSON_OBJ);
            // Add the product to the cart
            handler.mutate(products, VariableMutationType.EXTEND, product);
        });

        // Register an interrupt handler for when the checkout is completed
        wf.registerInterruptHandler("checkout-completed", handler -> {
            // Update the 'isCheckedOut' variable when the checkout is completed
            handler.mutate(isCheckedOut, VariableMutationType.ASSIGN, true);
        });
    }

    // Method to create and return a new workflow instance for registration
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WF_NAME, this::defineWorkflow);
    }
}
