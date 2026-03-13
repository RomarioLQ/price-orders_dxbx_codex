package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Заказ клиента поставщику.
 */
@Getter
@Setter
@Entity
@Table(name = "system_orders")
public class SystemOrder {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;

  @Column(name = "address_id", nullable = false)
  private UUID addressId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "datetime", nullable = false)
  private LocalDateTime datetime;

  /**
   * Номер заказа (номер документа из внешней системы).
   */
  @Column(name = "number", length = 100)
  private String number;

  @Column(name = "status")
  private Boolean status;
}
