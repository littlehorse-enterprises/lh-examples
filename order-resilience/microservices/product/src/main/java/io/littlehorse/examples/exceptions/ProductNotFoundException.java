package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.VariableValue;

public class ProductNotFoundException extends LHTaskException {
    public ProductNotFoundException(String message, VariableValue variableValue) {
        super("product-not-found", message, variableValue);
    }
}
