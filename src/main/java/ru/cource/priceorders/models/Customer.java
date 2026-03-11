package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Клиент (юридическое лицо).
 */
@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "inn", nullable = false)
  private String inn;

  @Column(name = "kpp")
  private String kpp;

  /**
   * GUID клиента в учетной системе поставщика.
   */
  @Column(name = "system_guid")
  private UUID systemGuid;
}
