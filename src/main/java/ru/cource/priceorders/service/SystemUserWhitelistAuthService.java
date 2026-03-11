package ru.cource.priceorders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.SystemUserRepository;
import ru.cource.priceorders.models.SystemUser;

import java.util.Optional;

/**
 * Авторизация по белому списку: если user найден по телефону -
 * записывает tg_user_id и возвращает true, иначе false.
 */
@Service
@RequiredArgsConstructor
public class SystemUserWhitelistAuthService {

  private final SystemUserRepository systemUserRepository;

  @Transactional
  public boolean authorize(String phone, String tgUserId) {
    String normalizedPhone = Optional.ofNullable(phone).orElse("").trim();
    String normalizedTgUserId = Optional.ofNullable(tgUserId).orElse("").trim();

    return systemUserRepository.findFirstByPhone(normalizedPhone)
        .map(u -> {
          u.setTgUserId(normalizedTgUserId);
          systemUserRepository.save(u);
          return true;
        })
        .orElse(false);
  }
}
