package io.littlehorse.ledger;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.littlehorse.ledger.transaction.Transaction;
import io.socket.client.IO;
import io.socket.client.Socket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class NotificationsService {
  private Socket socket;
  @Autowired
  private ObjectMapper objectMapper;
  Logger logger = LoggerFactory.getLogger(NotificationsService.class);

  @Autowired
  public NotificationsService(@Value("${notifications-service}") final String socketUrl) {
    IO.Options options = IO.Options.builder()
        .setForceNew(false)
        .build();
    logger.info("Socket initialized " + socketUrl);
    this.socket = IO.socket(URI.create(socketUrl), options);
  }

  public void publishTransaction(String namespace, Transaction transaction) {
    logger.info("Emiting event: " + namespace);
    try {
      String message = objectMapper.writeValueAsString(transaction);
      socket.emit(namespace, message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Couldn't serialize transaction");
    }
  }

  @PostConstruct
  public void connect() {
    this.socket.connect();
  }

  @PreDestroy
  public void close() {
    this.socket.close();
  }
}
