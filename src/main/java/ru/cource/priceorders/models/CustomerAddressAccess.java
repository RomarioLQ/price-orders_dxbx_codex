package ru.cource.priceorders.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Связь клиент -> адрес (customer_address_access).
 */
@Getter
@Setter
@Entity
@Table(name = "customer_address_access")
public class CustomerAddressAccess {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "address_id", nullable = false)
  private UUID addressId;
}
