package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.UserCustomerId;

import java.util.Optional;
import java.util.UUID;

public interface UserCustomerIdRepository extends JpaRepository<UserCustomerId, UUID> {

  Optional<UserCustomerId> findFirstByUserId(UUID userId);
}
