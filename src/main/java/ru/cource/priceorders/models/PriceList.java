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
 * Прайс-лист поставщика для конкретного клиента.
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

  @Column(name = "datetime", nullable = false)
  private LocalDateTime datetime;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

}
