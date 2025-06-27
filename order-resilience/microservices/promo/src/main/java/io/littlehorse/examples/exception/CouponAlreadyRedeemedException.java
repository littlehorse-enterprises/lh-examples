package io.littlehorse.examples.exception;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHTaskException;

public class CouponAlreadyRedeemedException extends LHTaskException {
    public CouponAlreadyRedeemedException(String message) {
        super("coupon-already-reedemed",message, LHLibUtil.objToVarVal(message));
    }
}
