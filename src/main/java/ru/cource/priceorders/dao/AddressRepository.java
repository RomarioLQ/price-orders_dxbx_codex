package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cource.priceorders.dao.projection.CustomerAddressSearchSelectProjection;
import ru.cource.priceorders.models.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

  @Query(value = """
      SELECT
        a.id AS id,
        a.name AS name
      FROM address a
      WHERE a.customer_id = :customerId
        AND (
             :searchString IS NULL OR :searchString = ''
             OR a.name ILIKE CONCAT('%', :searchString, '%')
        )
      ORDER BY a.name
      LIMIT :limit
      """, nativeQuery = true)
  List<CustomerAddressSearchSelectProjection> searchSelect(
      @Param("customerId") UUID customerId,
      @Param("searchString") String searchString,
      @Param("limit") int limit
  );
}
