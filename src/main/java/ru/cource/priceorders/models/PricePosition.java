package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Позиция прайс-листа (цена/ед.изм/НДС) ...
 */
@Getter
@Setter
@Entity
@Table(name = "price_positions")
public class PricePosition {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "price_id", nullable = false)
  private UUID priceId;

  @Column(name = "nomenclature_id", nullable = false)
  private UUID nomenclatureId;

  @Column(name = "unit_measure")
  private String unitMeasure;

  @Column(name = "price_with_vat", nullable = false)
  private BigDecimal priceWithVat;

  @Column(name = "vat", nullable = false)
  private Long vat;
}
