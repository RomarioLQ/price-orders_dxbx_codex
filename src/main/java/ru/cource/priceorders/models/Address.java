package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Адрес точки клиента.
 */
@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "address", nullable = false, length = 500)
  private String address;
}
