package ru.cource.priceorders.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.service.SystemUserWhitelistAuthService;

/**
 * Авторизация по белому списку (по телефону).
 */
@RestController
@RequestMapping("/api/v1/system-users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SystemUserWhitelistAuthFrontController {

  private final SystemUserWhitelistAuthService service;

  /**
   * Если пользователь найден по телефону - записывает tg_user_id и возвращает true, иначе false.
   */
  @GetMapping("/whitelist-auth")
  public boolean whitelistAuth(
      @RequestParam("phone") @NotBlank String phone,
      @RequestParam("tgUserId") @NotBlank @Size(max = 50) String tgUserId
  ) {
    long startedAt = System.currentTimeMillis();
    String maskedPhone = maskPhone(phone);
    log.info(
        "whitelistAuth started. phone={}, tgUserId={}",
        maskedPhone,
        tgUserId
    );
    try {
      boolean authorized = service.authorize(phone, tgUserId);
      log.info(
          "whitelistAuth completed. authorized={}, phone={}, tgUserId={}, durationMs={}",
          authorized,
          maskedPhone,
          tgUserId,
          System.currentTimeMillis() - startedAt
      );
      return authorized;
    } catch (RuntimeException ex) {
      log.error(
          "whitelistAuth failed. phone={}, tgUserId={}, durationMs={}",
          maskedPhone,
          tgUserId,
          System.currentTimeMillis() - startedAt,
          ex
      );
      throw ex;
    }
  }

  private static String maskPhone(String phone) {
    if (phone == null || phone.isBlank()) {
      return "<empty>";
    }
    int length = phone.length();
    if (length <= 4) {
      return "***" + phone;
    }
    return "***" + phone.substring(length - 4);
  }
}
