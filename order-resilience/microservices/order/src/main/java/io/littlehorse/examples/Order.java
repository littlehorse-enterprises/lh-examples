package io.littlehorse.examples;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private int id;

  private int clientId;
  
  private String error;
  
  private double totalPrice;
  
  @Enumerated(EnumType.STRING)
  private OrderStatus status = OrderStatus.PENDING;
  
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

/**
 * The status of an order
 */
enum OrderStatus {
  PENDING,
  COMPLETED,
  CANCELLED
} 