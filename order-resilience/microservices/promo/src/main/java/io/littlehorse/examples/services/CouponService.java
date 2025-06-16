package io.littlehorse.examples.services;

import io.littlehorse.examples.exception.CouponAlreadyCreatedException;
import io.littlehorse.examples.exception.CouponAlreadyRedeemedException;
import io.littlehorse.examples.model.Coupon;
import io.littlehorse.examples.workflows.CouponWorkflow;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.littlehorse.examples.repositories.CouponRepository;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CouponService {
    CouponRepository couponRepository;

    private final LittleHorseBlockingStub blockingStub;

    public CouponService(CouponRepository couponRepository, LittleHorseBlockingStub blockingStub) {
        this.couponRepository = couponRepository;
        this.blockingStub = blockingStub;
    }
    @Transactional
    public void generateCoupon(Long clientId, Long productId, String productName) {
        Coupon existingCoupon = couponRepository.findByClientIdAndProductId(clientId, productId);
        if (existingCoupon != null) {
            throw new CouponAlreadyCreatedException("Coupon already exists for clientId: " + clientId + " and productId: " + productId);
        }
        Coupon coupon = Coupon.builder()
                .clientId(clientId)
                .productId(productId)
                .code("COUPON-" + clientId + "-" + productId + "-" + productName)
                .description("Coupon for " + productName + " with a discount of 20% that can be applied to the next single purchase.")
                .discountPercentage(20) // Default discount percentage
                .redeemed(false)
                .build();
        couponRepository.persist(coupon);
    }

    @Transactional
    public Coupon[] useCoupons(String[] codes) {
        if(codes == null || codes.length == 0) {
            return new Coupon[0]; // No coupons to redeem
        }
        var coupons= couponRepository.listByCouponCodes(Arrays.asList(codes));
        // validate unique coupon codes
        if (coupons.size() != codes.length) {
            List<String> redeemedCodes = new ArrayList<>();
            for (Coupon c : coupons) {
                redeemedCodes.add(c.getCode());
            }
            throw new CouponAlreadyRedeemedException("Some coupons are already redeemed or inactive: " + redeemedCodes);
        }

        // Mark all coupons as redeemed
        for (Coupon coupon : coupons) {
            coupon.setRedeemed(true);
            couponRepository.persist(coupon);
        }
        return coupons.toArray(Coupon[]::new);
    }

    public List<Coupon> getAllCoupnsForClient(Long clientId) {
        return couponRepository.listActiveByClientId(clientId);
    }

    public void runGenerateCouponWorkflow(Long clientId, Long productId, String productName) {
//        String wfRunId = "coupon-" + clientId + "-" + productId + "-" + productName.toLowerCase();
        String wfRunId = UUID.randomUUID().toString().replace("-", "");
        RunWfRequest request = RunWfRequest.newBuilder()
                .setWfSpecName(CouponWorkflow.WORKFLOW_NAME)
                .putVariables(CouponWorkflow.CLIENT_ID, LHLibUtil.objToVarVal(clientId))
                .putVariables(CouponWorkflow.PRODUCT_ID, LHLibUtil.objToVarVal(productId))
                .putVariables(CouponWorkflow.PRODUCT_NAME, LHLibUtil.objToVarVal(productName))
                .setId(wfRunId)
                .build();
        blockingStub.runWf(request);

    }

}
