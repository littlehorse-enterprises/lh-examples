package io.littlehorse.examples;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;


import java.util.List;


@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {
    public List<Order> findByClientId(int clientId) {
        return find("clientId", clientId).list();
    }
}
