package io.littlehorse.ledger;

import java.io.IOException;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import jakarta.annotation.PostConstruct;

public class OutboxWorkflow {
  private LittleHorseBlockingStub client;

  public OutboxWorkflow(LittleHorseBlockingStub client) {
    this.client = client;
  }

  private void registerWorkflow() {
    Workflow workflow = new WorkflowImpl("outbox", wf -> {
      WfRunVariable account = wf.addVariable("account", VariableType.STR).required().searchable();
      WfRunVariable total = wf.addVariable("total", VariableType.DOUBLE).required();
      WfRunVariable sku = wf.addVariable("sku", VariableType.STR).required().searchable();
      WfRunVariable quantity = wf.addVariable("quantity", VariableType.INT).required();
      WfRunVariable transactionId = wf.addVariable("transactionId", VariableType.STR);

      NodeOutput paymentTxId = wf.execute("process-payment", account, total);
      wf.mutate(transactionId, VariableMutationType.ASSIGN, paymentTxId);

      NodeOutput shipmentTxId = wf.execute("ship-item", sku, quantity, transactionId);

      wf.handleException(shipmentTxId, "out-of-stock", handler -> {
        handler.execute("issue-refund", transactionId);
        handler.fail("out-of-stock", "not enough inventory to complete transaction");
      });
    });

    workflow.registerWfSpec(client);
  }

  @PostConstruct
  public void register() throws IOException {
    this.registerWorkflow();
  }
}
