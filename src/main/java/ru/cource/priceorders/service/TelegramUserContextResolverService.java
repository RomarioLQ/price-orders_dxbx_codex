package ru.cource.priceorders.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.SystemUserRepository;
import ru.cource.priceorders.dao.UserCustomerIdRepository;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.UserCustomerId;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelegramUserContextResolverService {

  private final SystemUserRepository systemUserRepository;
  private final UserCustomerIdRepository userCustomerIdRepository;
  private final TraceIdGenerator traceIdGenerator;

  public TelegramUserContext resolve(TelegramUserData telegramUserData) {
    String tgUserId = Optional.ofNullable(telegramUserData)
        .map(TelegramUserData::tgUserId)
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .orElseThrow(() -> validation("telegramUserData.tgUserId", "Telegram user id is required"));

    UUID userId = systemUserRepository.findFirstByTgUserId(tgUserId)
        .map(systemUser -> systemUser.getId())
        .orElseThrow(() -> new ValidationException(
            "SYSTEM_USER_NOT_FOUND",
            traceIdGenerator.gen(),
            "System user is not found for provided Telegram user id",
            null,
            List.of(ParamDto.builder().key("tgUserId").value(tgUserId).build())
        ));

    UUID customerId = userCustomerIdRepository.findFirstByUserId(userId)
        .map(UserCustomerId::getCustomerId)
        .orElseThrow(() -> new ValidationException(
            "CUSTOMER_NOT_FOUND",
            traceIdGenerator.gen(),
            "Customer is not found for provided userId",
            null,
            List.of(ParamDto.builder().key("userId").value(userId.toString()).build())
        ));

    return new TelegramUserContext(userId, customerId);
  }

  private ValidationException validation(String key, String message) {
    return new ValidationException(
        "PARAMS_VALIDATION_FAILED",
        traceIdGenerator.gen(),
        message,
        null,
        List.of(ParamDto.builder().key(key).value(message).build())
    );
  }

  public record TelegramUserContext(UUID userId, UUID customerId) {
  }
}
