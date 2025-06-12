package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.common.proto.VariableValue;

public class InsufficientStockException extends LHTaskException {
    public InsufficientStockException(String message) {
        super("out-of-stock",message, LHLibUtil.objToVarVal(message));
    }
}
