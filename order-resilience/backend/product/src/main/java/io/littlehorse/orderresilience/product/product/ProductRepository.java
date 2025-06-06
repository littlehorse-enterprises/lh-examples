package io.littlehorse.orderresilience.product.product;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer> {
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchCriteria% OR p.description LIKE %:searchCriteria% OR p.category LIKE %:searchCriteria%")
    List<Product> findBySearchCriteria(@Param("searchCriteria") String searchCriteria);
    
    List<Product> findByStockGreaterThanEqual(int minStock);
}
