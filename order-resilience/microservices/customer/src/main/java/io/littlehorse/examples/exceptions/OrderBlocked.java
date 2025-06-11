package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.VariableValue;

public class OrderBlocked extends LHTaskException {
    public OrderBlocked(String message, VariableValue variableValue) {
        super("order-blocked",message,variableValue);
    }
}
