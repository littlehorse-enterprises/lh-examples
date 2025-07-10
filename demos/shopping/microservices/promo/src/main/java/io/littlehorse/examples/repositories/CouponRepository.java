package io.littlehorse.examples.repositories;

import io.littlehorse.examples.model.Coupon;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CouponRepository implements PanacheRepository<Coupon> {
    public Coupon findByClientIdAndProductId(Long clientId, Long productId) {
        return find("clientId = ?1 and productId = ?2", clientId, productId).firstResult();
    }
    public List<Coupon> listByClientId(Long clientId) {
        return find("clientId = ?1", clientId).list();
    }
    public List<Coupon> listByCouponCodes(List<String> couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return List.of();
        }
        return find("code in ?1", couponCode).list();
    }
}
