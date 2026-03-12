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

  Optional<Address> findFirstByCustomerIdAndAdditionalId(UUID customerId, String additionalId);

  @Query(value = """
      SELECT
        a.id AS id,
        COALESCE(a.name, a.additional_id) AS address
      FROM address a
      WHERE a.customer_id = :customerId
        AND (
             :searchString IS NULL OR :searchString = ''
             OR COALESCE(a.name, '') ILIKE CONCAT('%', :searchString, '%')
             OR a.additional_id ILIKE CONCAT('%', :searchString, '%')
        )
      ORDER BY COALESCE(a.name, a.additional_id)
      LIMIT :limit
      """, nativeQuery = true)
  List<CustomerAddressSearchSelectProjection> searchSelect(
      @Param("customerId") UUID customerId,
      @Param("searchString") String searchString,
      @Param("limit") int limit
  );
}
