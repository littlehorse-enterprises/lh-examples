package io.littlehorse.orderresilience.customer.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:searchCriteria% or c.email LIKE %:searchCriteria%")
    List<Customer> findBySearchCriteria(@Param("searchCriteria") String searchCriteria);
}
