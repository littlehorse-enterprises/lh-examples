package io.littlehorse.ledger;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;

@Service
public class WorkflowService {
  private LittleHorseBlockingStub client;

  public WorkflowService(@Autowired LHConfig config) {
    this.client = config.getBlockingStub();
  }

  public void runWorkflow(CommerceData commerce) {
    Double total = commerce.getPrice().multiply(BigDecimal.valueOf(commerce.getQuantity())).doubleValue();
    RunWfRequest request = RunWfRequest.newBuilder().setWfSpecName("outbox")
        .putVariables("account", LHLibUtil.objToVarVal(commerce.getGifcard()))
        .putVariables("sku", LHLibUtil.objToVarVal(commerce.getSku()))
        .putVariables("total", LHLibUtil.objToVarVal(total))
        .putVariables("quantity", LHLibUtil.objToVarVal(commerce.getQuantity()))
        .build();
    client.runWf(request);
  }
}
