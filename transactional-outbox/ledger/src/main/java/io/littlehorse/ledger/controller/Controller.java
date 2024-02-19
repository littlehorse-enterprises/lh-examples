package io.littlehorse.ledger.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.littlehorse.ledger.CommerceData;
import io.littlehorse.ledger.WorkflowService;
import io.littlehorse.ledger.transaction.BalanceProjection;
import io.littlehorse.ledger.transaction.Transaction;
import io.littlehorse.ledger.transaction.TransactionService;

@RestController
public class Controller {
  @Autowired
  private TransactionService transactionService;

  @Autowired
  private WorkflowService workflowService;

  @GetMapping("/transaction/{id}")
  @ResponseStatus(code = HttpStatus.OK)
  public Transaction getTransactionById(@PathVariable UUID id) {
    return transactionService.get(id);
  }

  @GetMapping("/balance/{account}")
  @ResponseStatus(code = HttpStatus.OK)
  public BalanceProjection getAccountBalance(@PathVariable String account) {
    return transactionService.getBalance(account);
  }

  @GetMapping("/balance")
  public List<BalanceProjection> getBalances() {
    return transactionService.getBalances();
  }

  @PostMapping("/transaction/credit")
  @ResponseStatus(code = HttpStatus.CREATED)
  public Transaction createTransaction(@RequestBody Transaction transaction) {
    return transactionService.credit(
        transaction.getAccount(),
        transaction.getAmount(),
        transaction.getIdempotencyKey());
  }

  @PostMapping("/run-workflow")
  public void runWorkflow(@RequestBody CommerceData commerce)
  {
    workflowService.runWorkflow(commerce);
  }

}
