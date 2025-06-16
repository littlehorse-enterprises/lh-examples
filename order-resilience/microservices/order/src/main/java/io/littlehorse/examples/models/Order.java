package io.littlehorse.examples.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private int orderId;

  private int clientId;

  @Column(columnDefinition = "TEXT")
  private String message;
  
  private double total;
  
  private String status;

  private String discountCodes;
  
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private List<OrderLine> orderLines = new ArrayList<>();
  
  public void addOrderLine(OrderLine orderLine) {
    orderLines.add(orderLine);
    orderLine.setOrder(this);
  }
  
  public void removeOrderLine(OrderLine orderLine) {
    orderLines.remove(orderLine);
    orderLine.setOrder(null);
  }
}
