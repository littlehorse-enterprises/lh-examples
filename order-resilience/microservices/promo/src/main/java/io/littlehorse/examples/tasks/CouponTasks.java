package io.littlehorse.examples.tasks;

import io.littlehorse.examples.model.Coupon;
import io.littlehorse.examples.services.CouponService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Date;

@LHTask
public class CouponTasks {
    public static final String CREATE_COUPON = "create-coupon";
    public static final String REDEEM_COUPONS = "redeem-coupons";
    public static final String GET_COUPONS_BY_CODES = "get-coupons";
    private static final Logger LOG = Logger.getLogger(CouponTasks.class);

    @Inject
    CouponService couponService;

    @LHTaskMethod(GET_COUPONS_BY_CODES)
    public Coupon[] getCouponsByCodes(int clientId, String[] couponCodes, WorkerContext workerContext) {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Getting coupons for %d at %s ", clientId, startTime);
        var response = this.couponService.getCouponsByCodes(clientId, couponCodes);
        var endTime = new Date();
        LOG.infof("Getting coupons for %d at %s , took %d ms", clientId, endTime, endTime.getTime() - startTime.getTime());
        return response;
    }

    @LHTaskMethod(REDEEM_COUPONS)
    public Coupon[] redeemCoupon(int clientId, String[] couponCodes, WorkerContext workerContext) {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Redeeming coupons for %d at %s ", clientId, startTime);
        var response = this.couponService.redeemCoupons(clientId, couponCodes);
        var endTime = new Date();
        LOG.infof("Redeemed coupons for %d at %s , took %d ms", clientId, endTime, endTime.getTime() - startTime.getTime());
        return response;
    }

    @LHTaskMethod(CREATE_COUPON)
    public Coupon generateCoupon(Long clientId, Long productId, String productName, WorkerContext workerContext) {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Creating coupons for %d at %s ", clientId, startTime);
        var response = this.couponService.generateCoupon(clientId, productId, productName);
        var endTime = new Date();
        LOG.infof("Created coupons for %d at %s , took %d ms", clientId, endTime, endTime.getTime() - startTime.getTime());
        return response;
    }

}
