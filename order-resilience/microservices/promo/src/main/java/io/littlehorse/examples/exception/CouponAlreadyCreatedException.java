package io.littlehorse.examples.exception;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHTaskException;

public class CouponAlreadyCreatedException extends LHTaskException {
    public CouponAlreadyCreatedException(String message) {
        super("coupon-already-created",message, LHLibUtil.objToVarVal(message));
    }
}
