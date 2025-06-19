package io.littlehorse.examples.services;

import io.littlehorse.examples.exception.CouponAlreadyCreatedException;
import io.littlehorse.examples.exception.CouponAlreadyRedeemedException;
import io.littlehorse.examples.model.Coupon;
import io.littlehorse.examples.workflows.CouponWorkflow;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.littlehorse.examples.repositories.CouponRepository;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import jakarta.annotation.PostConstruct;


import java.util.*;

@ApplicationScoped
@Startup
public class CouponService {
    CouponRepository couponRepository;

    private final LittleHorseBlockingStub blockingStub;

    public CouponService(CouponRepository couponRepository, LittleHorseBlockingStub blockingStub) {
        this.couponRepository = couponRepository;
        this.blockingStub = blockingStub;
    }

    @PostConstruct
    void enableOutputTopic() {
        System.out.println("Enabling output topic for default tenant");
        this.blockingStub.putTenant(PutTenantRequest.newBuilder().setId("default").setOutputTopicConfig(OutputTopicConfig.newBuilder()).build());
    }

    @Transactional
    public void generateCoupon(Long clientId, Long productId, String productName) {
        Coupon existingCoupon = couponRepository.findByClientIdAndProductId(clientId, productId);
        if (existingCoupon != null) {
            throw new CouponAlreadyCreatedException("Coupon already exists for clientId: " + clientId + " and productId: " + productId);
        }
        var code = "COUPON-" + clientId + "-" + productId + "-" + productName;
        var discountPercentage = (new Random().nextInt(7) + 1) * 10; // Random discount percentage between 10 and 70
        Coupon coupon = Coupon.builder()
                .clientId(clientId)
                .productId(productId)
                .code(code.toUpperCase())
                .description("Coupon for " + productName + " with a discount of " + discountPercentage + "% , you can use it in the next order :)")
                .discountPercentage(discountPercentage) // Default discount percentage
                .redeemed(false)
                .build();
        couponRepository.persist(coupon);
    }

    @Transactional
    public Coupon[] redeemCoupons(int clientId, String[] codes) {
        if (codes == null || codes.length == 0) {
            return new Coupon[0]; // No coupons to redeem
        }
        var coupons = getCouponsByCodes(clientId, codes);


        // Mark all coupons as redeemed
        for (Coupon coupon : coupons) {
            coupon.setRedeemed(true);
            couponRepository.persist(coupon);
        }
        return coupons;
    }

    public List<Coupon> getAllCoupnsForClient(Long clientId) {
        return couponRepository.listByClientId(clientId);
    }

    @Transactional
    public Coupon[] getCouponsByCodes(int clientId, String[] codes) {
        if (codes == null || codes.length == 0) {
            return new Coupon[0]; // No coupons to retrieve
        }
        var coupons = couponRepository.listByCouponCodes(Arrays.asList(codes));
        // validate unique coupon codes or existence
        List<String> invalidCodes = new ArrayList<>();
        for (String code : codes) {
            var coupon = coupons.stream().filter(c -> c.getCode().equals(code)).findFirst();
            if (coupon.isEmpty() || coupon.get().getClientId() != clientId || coupon.get().isRedeemed()) {
                invalidCodes.add(code);
            }
        }
        if (!invalidCodes.isEmpty()) {
            throw new CouponAlreadyRedeemedException("Coupons already redeemed or not found for codes: " + invalidCodes);
        }
        return couponRepository.listByCouponCodes(Arrays.asList(codes)).toArray(Coupon[]::new);
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
