package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Связь пользователей (system_users) с клиентом (customer).
 */
@Getter
@Setter
@Entity
@Table(name = "user_customer_id")
public class UserCustomerId {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;
}
