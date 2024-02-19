package io.littlehorse.ledger.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "account")
  private String account;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "idempotency_key")
  private String idempotencyKey;

  public Transaction(String account, BigDecimal amount, String idempotencyKey) {
    this.account = account;
    this.amount = amount;
    this.idempotencyKey = idempotencyKey;
  }

  public Transaction(String account, Double amount, String idempotencyKey) {
    this.account = account;
    this.amount = BigDecimal.valueOf(amount);
    this.idempotencyKey = idempotencyKey;
  }


}
