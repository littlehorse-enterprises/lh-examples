package io.littlehorse.ledger.controller;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.littlehorse.ledger.transaction.exceptions.AmountMismatch;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler({JpaSystemException.class, AmountMismatch.class})
  protected ResponseEntity<ErrorResponse> handleNotPostgresExceptions(Exception e) {
    String message = e.getMessage();
    return new ResponseEntity<ErrorResponse>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
  }
}

@Data
@RequiredArgsConstructor
class ErrorResponse {
  @NonNull
  private String message;
}
