package io.littlehorse.ledger.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
  Transaction getById(UUID id);

  @Query(value = "SELECT * FROM balances WHERE account = ?1", nativeQuery = true)
  BalanceProjection getBalanceByAccount(String account);

  @Query(value = "SELECT * FROM balances", nativeQuery = true)
  List<BalanceProjection> getBalances();

  Optional<Transaction> getByAccountAndIdempotencyKey(String account, String idempotencyKey);
}
