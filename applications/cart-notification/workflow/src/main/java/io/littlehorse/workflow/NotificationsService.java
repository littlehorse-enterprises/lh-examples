package io.littlehorse.workflow;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.socket.client.IO;
import io.socket.client.Socket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class NotificationsService {
  private Socket socket;
  Logger logger = LoggerFactory.getLogger(NotificationsService.class);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  public NotificationsService(@Value("${notifications-service}") final String socketUrl) {
    IO.Options options = IO.Options.builder()
        .setForceNew(false)
        .build();
    logger.info("Socket initialized " + socketUrl);
    this.socket = IO.socket(URI.create(socketUrl), options);
  }

  public void publishMessage(String namespace, Message message) {
    logger.info("Emiting event: " + namespace);
    try {
      String msg = objectMapper.writeValueAsString(message);
      socket.emit(namespace, msg);
    } catch (Exception e) {
      logger.error("Error publishing message", e);
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
