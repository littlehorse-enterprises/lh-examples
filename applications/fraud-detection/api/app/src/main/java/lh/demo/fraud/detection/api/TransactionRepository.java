package lh.demo.fraud.detection.api;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
    Transaction getById(UUID id);
}
