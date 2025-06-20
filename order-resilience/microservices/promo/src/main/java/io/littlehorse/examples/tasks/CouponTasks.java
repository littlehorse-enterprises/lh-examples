package io.littlehorse.examples.tasks;

import io.littlehorse.examples.model.Coupon;
import io.littlehorse.examples.services.CouponService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import jakarta.inject.Inject;

@LHTask
public class CouponTasks {
    public static final String CREATE_COUPON = "create-coupon";
    public static final String REDEEM_COUPONS = "redeem-coupons";
    public static final String GET_COUPONS_BY_CODES = "get-coupons";


    @Inject
    CouponService couponService;

    @LHTaskMethod(GET_COUPONS_BY_CODES)
    public Coupon[] getCouponsByCodes(int clientId, String[] couponCodes, WorkerContext workerContext) {
        return this.couponService.getCouponsByCodes(clientId, couponCodes);
    }

    @LHTaskMethod(REDEEM_COUPONS)
    public Coupon[] redeemCoupon(int clientId, String[] couponCodes) {
        return this.couponService.redeemCoupons(clientId, couponCodes);
    }

    @LHTaskMethod(CREATE_COUPON)
    public Coupon generateCoupon(Long clientId, Long productId, String productName) {
        return this.couponService.generateCoupon(clientId, productId, productName);
    }

}
