package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.VariableValue;

public class InsufficientStockException extends LHTaskException {
    public InsufficientStockException(String message, VariableValue variableValue) {
        super("out-of-stock",message,variableValue);
    }
}
