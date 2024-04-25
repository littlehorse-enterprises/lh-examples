package io.littlehorse.workflow;

import java.io.IOException;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import jakarta.annotation.PostConstruct;

public class CartWorkflow {
  private LittleHorseBlockingStub client;
  public static final String WF_NAME = "cart-management";
  public static final String NOTIFY_CART_TASK = "notify-cart";
  public static final String NOTIFY_ADD_TO_CART = "notify-add-to-cart";
  public static final String NOTIFY_CHECKOUT = "notify-checkout";
  public static final String CHECKOUT_INTERUPT = "checkout-completed";
  public static final String ADD_TO_CART_INTERUPT = "add-to-cart";

  public CartWorkflow(LittleHorseBlockingStub client) {
    this.client = client;
  }

  private void registerWorkflow() {
    Workflow workflow = new WorkflowImpl(WF_NAME, wf -> {
      // Account variable to track the user account
      WfRunVariable account = wf.addVariable("account", VariableType.STR);

      // Variable to track if the cart has been checked out
      WfRunVariable isCheckedOut = wf.addVariable("isCheckedOut", false);

      // Input variable to represent product being added to cart
      WfRunVariable products = wf.addVariable("products", VariableType.JSON_ARR).searchable();

      wf.doWhile(
          wf.condition(isCheckedOut, Comparator.EQUALS, false),
          loopBody -> {
            loopBody.sleepSeconds(60); // Wait for 1 minute
            loopBody.execute(NOTIFY_CART_TASK, account);
          });

      // Register an interrupt handler to modify the cart items
      wf.registerInterruptHandler(ADD_TO_CART_INTERUPT, handler -> {
        WfRunVariable newProducts = handler.addVariable(WorkflowThread.HANDLER_INPUT_VAR, VariableType.JSON_ARR);
        handler.execute(NOTIFY_ADD_TO_CART, account);
        // Add the product to the cart
        handler.mutate(products, VariableMutationType.ASSIGN, newProducts);
      });

      // Register an interrupt handler for when the checkout is completed
      wf.registerInterruptHandler(CHECKOUT_INTERUPT, handler -> {
        handler.execute(NOTIFY_CHECKOUT, account);
        // Update the 'isCheckedOut' variable when the checkout is completed
        handler.mutate(isCheckedOut, VariableMutationType.ASSIGN, true);
      });
    });

    workflow.registerWfSpec(client);
  }

  private void registerExternalEvents() {
    String[] externalEvents = { CHECKOUT_INTERUPT, ADD_TO_CART_INTERUPT };

    for (String event : externalEvents) {
      client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName(event).build());
    }
  }

  @PostConstruct
  public void register() throws IOException {
    this.registerExternalEvents();
    this.registerWorkflow();
  }
}
