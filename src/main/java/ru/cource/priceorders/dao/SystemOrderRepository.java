package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cource.priceorders.models.SystemOrder;

import java.util.List;
import java.util.UUID;

public interface SystemOrderRepository extends JpaRepository<SystemOrder, UUID> {

  @Query("""
      select o
      from SystemOrder o
      where o.supplierId = :supplierId
        and (o.status = false or o.status is null)
      order by o.datetime asc
      """)
  List<SystemOrder> findAllUnprocessedBySupplierId(@Param("supplierId") UUID supplierId);

  boolean existsBySupplierIdAndNumber(UUID supplierId, String number);
}
