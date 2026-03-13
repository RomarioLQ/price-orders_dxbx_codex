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
    String normalizedPhone = normalizePhone(phone);
    String normalizedTgUserId = Optional.ofNullable(tgUserId).orElse("").trim();

    Optional<SystemUser> systemUser = systemUserRepository.findFirstByPhone(normalizedPhone);
    if (systemUser.isEmpty()) {
      return false;
    }

    SystemUser user = systemUser.get();
    if (systemUserRepository.existsByTgUserIdAndIdNot(normalizedTgUserId, user.getId())) {
      return false;
    }

    if (normalizedTgUserId.equals(user.getTgUserId())) {
      return true;
    }

    user.setTgUserId(normalizedTgUserId);
    systemUserRepository.save(user);
    return true;
  }

  private String normalizePhone(String phone) {
    return Optional.ofNullable(phone)
        .orElse("")
        .trim()
        .replace(" ", "")
        .replace("-", "")
        .replace("(", "")
        .replace(")", "");
  }
}
