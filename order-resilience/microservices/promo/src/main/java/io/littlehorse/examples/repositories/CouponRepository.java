package io.littlehorse.examples.repositories;

import io.littlehorse.examples.model.Coupon;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CouponRepository implements PanacheRepository<Coupon> {
    public Coupon findByClientIdAndProductId(Long clientId, Long productId) {
        return find("clientId = ?1 and productId = ?2", clientId, productId).firstResult();
    }
    public List<Coupon> listByClientId(Long clientId) {
        return list("clientId", clientId);
    }
}
