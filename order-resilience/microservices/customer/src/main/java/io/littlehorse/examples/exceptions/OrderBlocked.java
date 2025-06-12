package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.VariableValue;

public class OrderBlocked extends LHTaskException {
    public OrderBlocked(String message) {
        super("order-blocked", message, LHLibUtil.objToVarVal(message));
    }
}
