package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cource.priceorders.dao.projection.SupplierSearchSelectProjection;
import ru.cource.priceorders.models.Supplier;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

  @Query(value = """
      SELECT
        s.id AS id,
        s.name AS name
      FROM supplier s
      JOIN price_list pl ON pl.supplier_id = s.id
      WHERE pl.is_active = true
        AND (pl.customer_id = :customerId OR pl.customer_id IS NULL)
        AND (
             :searchString IS NULL OR :searchString = ''
             OR s.name ILIKE CONCAT('%', :searchString, '%')
        )
      GROUP BY s.id, s.name
      ORDER BY MAX(CASE WHEN pl.customer_id = :customerId THEN 1 ELSE 0 END) DESC,
               s.name
      LIMIT :limit
      """, nativeQuery = true)
  List<SupplierSearchSelectProjection> searchSelectForCustomer(
      @Param("customerId") UUID customerId,
      @Param("searchString") String searchString,
      @Param("limit") int limit
  );
}
