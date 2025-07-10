package io.littlehorse.customer.customer;

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
@Table(name = "customers")
public class Customer {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "firstname")
  private String firstname;

  @Column(name = "lastname")
  private String lastname;

  @Column(name = "email")
  private String email;

  public Customer(String firstname, String lastname, String email) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
  }
}
