package io.littlehorse.examples;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_lines")
public class OrderLine {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private int id;
  
  private int productId;
  
  private int quantity;
  
  private double unitPrice;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JsonBackReference
  private Order order;
  
  public double getSubtotal() {
    return unitPrice * quantity;
  }
}
