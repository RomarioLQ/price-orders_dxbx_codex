package ru.cource.priceorders.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.dao.projection.SupplierSearchSelectProjection;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.dto.SupplierSearchSelectResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Search-select по поставщикам для клиента (по Telegram initData).
 * Возвращает всех поставщиков, у которых есть прайс для текущего клиента.
 */
@Service
@RequiredArgsConstructor
public class SupplierSearchSelectService {

  @Value("${app.searchSelect.defaultLimit:50}")
  private int limit;

  private final TelegramUserContextResolverService telegramUserContextResolverService;
  private final SupplierRepository supplierRepository;

  public List<SupplierSearchSelectResponseDto> searchSelect(TelegramUserData telegramUserData, String searchString) {
    UUID customerId = telegramUserContextResolverService.resolve(telegramUserData).customerId();
    String normalizedSearch = Optional.ofNullable(searchString).orElse("").trim();

    List<SupplierSearchSelectProjection> rows = supplierRepository.searchSelectForCustomer(customerId, normalizedSearch, limit);
    return rows.stream()
        .map(r -> SupplierSearchSelectResponseDto.builder()
            .id(r.getId())
            .name(r.getName())
            .build())
        .toList();
  }
}
