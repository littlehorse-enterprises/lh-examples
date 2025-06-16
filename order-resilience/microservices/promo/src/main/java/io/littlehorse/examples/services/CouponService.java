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
                .description("Coupon for " + productName + " with a discount of 20%")
                .discountPercentage(20) // Default discount percentage
                .isActive(true) // Default active status
                .build();
        couponRepository.persist(coupon);
    }

    @Transactional
    public void useCoupon(String code) {
        Coupon coupon = couponRepository.find("code", code).firstResult();
        if (coupon != null && coupon.isActive()) {
            // Logic to apply the coupon
            coupon.setActive(false); // Mark the coupon as used
            couponRepository.persist(coupon);
        } else {
            throw new CouponAlreadyRedeemedException("Invalid or inactive coupon code: " + code);
        }
    }

    public List<Coupon> getAllCoupnsForClient(Long clientId) {
        return couponRepository.listByClientId(clientId);
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
