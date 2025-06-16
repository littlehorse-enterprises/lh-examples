package io.littlehorse.examples.tasks;

import io.littlehorse.examples.services.CouponService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;

@LHTask
public class CouponTasks {
    public static final String CREATE_COUPON = "create-coupon";
    public static final String REDEEM_COUPON = "redeem-coupon";

    @Inject
    CouponService couponService;

    @LHTaskMethod(REDEEM_COUPON)
    public void redeemCoupon(String couponCode) {
        this.couponService.useCoupon(couponCode);
    }
    @LHTaskMethod(CREATE_COUPON)
    public void generateCoupon(Long clientId, Long productId, String productName) {
        this.couponService.generateCoupon(clientId, productId, productName);
    }
}
