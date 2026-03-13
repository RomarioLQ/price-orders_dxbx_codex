package ru.cource.priceorders.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.PriceListRepository;
import ru.cource.priceorders.dao.PricePositionRepository;
import ru.cource.priceorders.dao.projection.PricePositionSearchSelectProjection;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.dto.PricePositionSearchSelectResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Бизнес-логика search-select по позициям прайс-листа.
 */
@Service
@RequiredArgsConstructor
public class PricePositionSearchSelectService {

  @Value("${app.searchSelect.defaultLimit:50}")
  private int limit;

  private final TelegramUserContextResolverService telegramUserContextResolverService;
  private final PriceListRepository priceListRepository;
  private final PricePositionRepository pricePositionRepository;
  private final TraceIdGenerator traceIdGenerator;

  public List<PricePositionSearchSelectResponseDto> searchSelect(
      UUID supplierId,
      TelegramUserData telegramUserData,
      String searchString
  ) {
    UUID customerId = telegramUserContextResolverService.resolve(telegramUserData).customerId();
    UUID priceId = resolveActivePriceListId(supplierId, customerId);

    String normalizedSearch = Optional.ofNullable(searchString).orElse("").trim();

    List<PricePositionSearchSelectProjection> rows = pricePositionRepository.searchSelect(
        priceId,
        supplierId,
        normalizedSearch,
        limit
    );

    return rows.stream()
        .map(r -> PricePositionSearchSelectResponseDto.builder()
            .priceId(r.getPriceId())
            .nomenclatureId(r.getNomenclatureId())
            .name(r.getName())
            .unitMeasure(r.getUnitMeasure())
            .priceWithVat(r.getPriceWithVat())
            .build())
        .toList();
  }

  private UUID resolveActivePriceListId(UUID supplierId, UUID customerId) {
    return priceListRepository.findLatestActiveCustomerPriceListId(supplierId, customerId)
        .or(() -> priceListRepository.findLatestActiveCommonPriceListId(supplierId))
        .orElseThrow(() -> new NotFoundException(
            "PRICE_LIST_NOT_FOUND",
            traceIdGenerator.gen(),
            "Active price list is not found for supplier/customer",
            null,
            List.of(
                ParamDto.builder().key("supplierId").value(supplierId.toString()).build(),
                ParamDto.builder().key("customerId").value(customerId.toString()).build()
            )
        ));
  }
}
