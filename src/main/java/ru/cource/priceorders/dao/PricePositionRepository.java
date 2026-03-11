package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cource.priceorders.dao.projection.PricePositionSearchSelectProjection;
import ru.cource.priceorders.models.PricePosition;

import java.util.List;
import java.util.UUID;

public interface PricePositionRepository extends JpaRepository<PricePosition, UUID> {

  @Query(value = """
      SELECT
        pp.price_id AS priceId,
        pp.nomenclature_id AS nomenclatureId,
        n.name AS name,
        pp.unit_measure AS unitMeasure,
        pp.price_with_vat AS priceWithVat
      FROM price_positions pp
      JOIN nomenclature n ON n.id = pp.nomenclature_id
      WHERE pp.price_id = :priceId
        AND n.supplier_id = :supplierId
        AND (
             :searchString IS NULL OR :searchString = ''
             OR n.name ILIKE CONCAT('%', :searchString, '%')
             OR n.code ILIKE CONCAT('%', :searchString, '%')
        )
      ORDER BY n.name
      LIMIT :limit
      """, nativeQuery = true)
  List<PricePositionSearchSelectProjection> searchSelect(
      @Param("priceId") UUID priceId,
      @Param("supplierId") UUID supplierId,
      @Param("searchString") String searchString,
      @Param("limit") int limit
  );
}
