package io.littlehorse.ledger.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.littlehorse.ledger.transaction.exceptions.AmountMismatch;
import jakarta.transaction.Transactional;

@Service
public class TransactionService {
  @Autowired
  private TransactionRepository repository;

  public Transaction get(UUID id) {
    return repository.getById(id);
  }

  public BalanceProjection getBalance(String account) {
    return repository.getBalanceByAccount(account);
  }

  public List<BalanceProjection> getBalances() {
    return repository.getBalances();
  }

  public Transaction debit(String account, BigDecimal amount, String idempotencyKey) {
    Transaction transaction = new Transaction(account, toDebit(amount), idempotencyKey);
    return wrapper(transaction);
  }

  public Transaction credit(String account, BigDecimal amount, String idempotencyKey) {
    Transaction transaction = new Transaction(account, toCredit(amount), idempotencyKey);
    return wrapper(transaction);
  }

  @Transactional
  public Transaction revert(UUID transactionId) {
    Transaction transaction = this.get(transactionId);
    return this.credit(transaction.getAccount(), transaction.getAmount().multiply(BigDecimal.valueOf(-1.0)), transactionId.toString());
  }

  private Transaction wrapper(Transaction transaction) {
    Optional<Transaction> tx = repository.getByAccountAndIdempotencyKey(transaction.getAccount(),
        transaction.getIdempotencyKey());

    if (tx.isPresent()) {
      if (tx.get().getAmount().compareTo(transaction.getAmount()) == 0) {
        return tx.get();
      } else {
        String message = String.format(
            "Transaction already exists although the amounts does not match %s > %s",
            transaction.getAmount(), tx.get().getAmount());
        throw new AmountMismatch(message);
      }
    } else {
      return repository.save(transaction);
    }
  }

  private BigDecimal toDebit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.valueOf(0.0)) <= 0) {
      return amount;
    }

    return amount.multiply(BigDecimal.valueOf(-1.0));
  }

  private BigDecimal toCredit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.valueOf(0.0)) >= 0) {
      return amount;
    }

    return amount.multiply(BigDecimal.valueOf(-1.0));
  }
}
