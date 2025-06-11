package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.VariableValue;

public class CustomerNotFoundException extends LHTaskException {
    public CustomerNotFoundException(String message, VariableValue variableValue) {
        super("customer-not-found",message,variableValue);
    }
}
