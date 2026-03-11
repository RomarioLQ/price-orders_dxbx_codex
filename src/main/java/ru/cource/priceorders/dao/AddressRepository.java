package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cource.priceorders.dao.projection.CustomerAddressSearchSelectProjection;
import ru.cource.priceorders.models.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

  Optional<Address> findFirstByAddress(String address);

  @Query(value = """
      SELECT
        a.id AS id,
        a.address AS address
      FROM customer_address_access caa
      JOIN address a ON a.id = caa.address_id
      WHERE caa.customer_id = :customerId
        AND (
             :searchString IS NULL OR :searchString = ''
             OR a.address ILIKE CONCAT('%', :searchString, '%')
        )
      ORDER BY a.address
      LIMIT :limit
      """, nativeQuery = true)
  List<CustomerAddressSearchSelectProjection> searchSelect(
      @Param("customerId") UUID customerId,
      @Param("searchString") String searchString,
      @Param("limit") int limit
  );
}
