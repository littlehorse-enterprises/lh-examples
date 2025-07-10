package io.littlehorse.examples.repositories;

import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository  implements PanacheRepository<Product> {
}
