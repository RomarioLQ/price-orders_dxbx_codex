package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cource.priceorders.models.PriceList;

import java.util.Optional;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceList, UUID> {

  Optional<PriceList> findFirstBySupplierIdAndCustomerIdOrderByStartDateDesc(UUID supplierId, UUID customerId);

  @Query(value = """
      SELECT pl.id
      FROM price_list pl
      WHERE pl.supplier_id = :supplierId
        AND pl.customer_id = :customerId
        AND pl.is_active = true
        AND pl.start_date <= CURRENT_TIMESTAMP
        AND (pl.end_date IS NULL OR pl.end_date >= CURRENT_TIMESTAMP)
      ORDER BY pl.start_date DESC, pl.end_date DESC NULLS FIRST
      LIMIT 1
      """, nativeQuery = true)
  Optional<UUID> findLatestActiveCustomerPriceListId(@Param("supplierId") UUID supplierId,
                                                     @Param("customerId") UUID customerId);

  @Query(value = """
      SELECT pl.id
      FROM price_list pl
      WHERE pl.supplier_id = :supplierId
        AND pl.customer_id IS NULL
        AND pl.is_active = true
        AND pl.start_date <= CURRENT_TIMESTAMP
        AND (pl.end_date IS NULL OR pl.end_date >= CURRENT_TIMESTAMP)
      ORDER BY pl.start_date DESC, pl.end_date DESC NULLS FIRST
      LIMIT 1
      """, nativeQuery = true)
  Optional<UUID> findLatestActiveCommonPriceListId(@Param("supplierId") UUID supplierId);
}
