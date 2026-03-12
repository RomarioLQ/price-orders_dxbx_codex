package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Внешний идентификатор клиента в учетной системе поставщика.
 */
@Getter
@Setter
@Entity
@Table(name = "customer_external_id")
public class CustomerExternalId {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;

  @Column(name = "customer_external_id", nullable = false)
  private UUID customerExternalId;
}
