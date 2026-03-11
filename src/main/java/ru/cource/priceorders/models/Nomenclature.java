package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Номенклатура поставщика (общая сущность, используется в прайсах и заказах).
 */
@Getter
@Setter
@Entity
@Table(name = "nomenclature")
public class Nomenclature {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;
}
