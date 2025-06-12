package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHTaskException;

public class OrderBlocked extends LHTaskException {
    public OrderBlocked(String message) {
        super("order-blocked", message, LHLibUtil.objToVarVal(message));
    }
}
