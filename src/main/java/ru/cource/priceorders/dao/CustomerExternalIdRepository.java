package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.CustomerExternalId;

import java.util.Optional;
import java.util.UUID;

public interface CustomerExternalIdRepository extends JpaRepository<CustomerExternalId, UUID> {

  Optional<CustomerExternalId> findFirstByCustomerIdAndSupplierId(UUID customerId, UUID supplierId);
}
