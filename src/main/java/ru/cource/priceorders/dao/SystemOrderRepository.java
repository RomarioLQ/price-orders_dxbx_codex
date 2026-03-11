package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.SystemOrder;

import java.util.List;
import java.util.UUID;

public interface SystemOrderRepository extends JpaRepository<SystemOrder, UUID> {

  List<SystemOrder> findAllBySupplierIdAndStatusFalse(UUID supplierId);

  boolean existsBySupplierIdAndNumber(UUID supplierId, String number);
}
