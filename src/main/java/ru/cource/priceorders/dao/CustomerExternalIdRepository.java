package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.CustomerExternalId;

import java.util.Optional;
import java.util.UUID;

public interface CustomerExternalIdRepository extends JpaRepository<CustomerExternalId, UUID> {

  boolean existsByCustomerIdAndSupplierId(UUID customerId, UUID supplierId);

  Optional<CustomerExternalId> findFirstBySupplierIdAndCustomerExternalId(UUID supplierId, UUID customerExternalId);
}
