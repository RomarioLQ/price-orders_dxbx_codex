package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.CustomerAddressAccess;

import java.util.UUID;

public interface CustomerAddressAccessRepository extends JpaRepository<CustomerAddressAccess, UUID> {

  boolean existsByCustomerIdAndAddressId(UUID customerId, UUID addressId);
}
