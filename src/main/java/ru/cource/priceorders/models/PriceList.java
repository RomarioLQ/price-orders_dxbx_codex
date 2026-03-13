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
 * Прайс-лист поставщика. Может быть:
 * - клиентским (customer_id != null)
 * - общим (customer_id == null).
 */
@Getter
@Setter
@Entity
@Table(name = "price_list")
public class PriceList {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;

  @Column(name = "customer_id")
  private UUID customerId;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;
}
