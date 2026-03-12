package ru.cource.priceorders.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.AddressRepository;
import ru.cource.priceorders.dao.projection.CustomerAddressSearchSelectProjection;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.dto.CustomerAddressSearchSelectResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Search-select адресов клиента по Telegram initData.
 */
@Service
@RequiredArgsConstructor
public class CustomerAddressSearchSelectService {

  @Value("${app.searchSelect.defaultLimit:50}")
  private int limit;

  private final TelegramUserContextResolverService telegramUserContextResolverService;
  private final AddressRepository addressRepository;

  public List<CustomerAddressSearchSelectResponseDto> searchSelect(
      TelegramUserData telegramUserData,
      String searchString
  ) {
    UUID customerId = telegramUserContextResolverService.resolve(telegramUserData).customerId();
    String normalizedSearch = Optional.ofNullable(searchString).orElse("").trim();

    List<CustomerAddressSearchSelectProjection> rows =
        addressRepository.searchSelect(customerId, normalizedSearch, limit);
    return rows.stream()
        .map(r -> CustomerAddressSearchSelectResponseDto.builder()
            .id(r.getId())
            .address(r.getName())
            .build())
        .toList();
  }
}
