package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Позиция заказа.
 */
@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "nomenclature_id", nullable = false)
  private UUID nomenclatureId;

  @Column(name = "count", nullable = false)
  private Integer count;

  @Column(name = "price", nullable = false)
  private Double price;

  @Column(name = "sum", nullable = false)
  private Double sum;
}
