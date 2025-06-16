package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHTaskException;

public class InvalidPriceException extends LHTaskException {
    public InvalidPriceException(String message) {
        super("invalid-price",message, LHLibUtil.objToVarVal(message));
    }
}
