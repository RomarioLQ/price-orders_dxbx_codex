package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.SystemUser;

import java.util.Optional;
import java.util.UUID;

public interface SystemUserRepository extends JpaRepository<SystemUser, UUID> {

  Optional<SystemUser> findFirstByPhone(String phone);

  Optional<SystemUser> findFirstByTgUserId(String tgUserId);
}
