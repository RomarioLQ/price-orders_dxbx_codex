package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.PriceList;

import java.util.Optional;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceList, UUID> {

  Optional<PriceList> findFirstBySupplierIdAndCustomerIdOrderByDatetimeDesc(UUID supplierId, UUID customerId);
}
