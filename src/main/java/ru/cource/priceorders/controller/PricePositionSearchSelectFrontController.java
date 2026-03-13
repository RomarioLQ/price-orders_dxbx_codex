package ru.cource.priceorders.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.filter.TelegramRequestAttributes;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.dto.PricePositionSearchSelectResponseDto;
import ru.cource.priceorders.service.PricePositionSearchSelectService;

import java.util.List;
import java.util.UUID;

/**
 * Front API для динамического поиска позиций прайс-листа (search-select).
 *
 * Используется Telegram Web App при вводе строки: на каждый набор символов
 * дергается этот метод и возвращается список подходящих позиций.
 */
@RestController
@RequestMapping("/api/v1/price-positions")
@RequiredArgsConstructor
@Validated
public class PricePositionSearchSelectFrontController {

  private final PricePositionSearchSelectService service;

  /**
   * Возвращает позиции прайс-листа, доступные для заказа, по введенной строке.
   *
   * Алгоритм выбора прайса:
   * 1) клиентский прайс (supplierId + customerId),
   * 2) общий прайс (supplierId + customerId = NULL).
   */
  @GetMapping("/search-select")
  public List<PricePositionSearchSelectResponseDto> searchSelect(
      HttpServletRequest request,
      @RequestParam("supplierId") @NotNull UUID supplierId,
      @RequestParam(value = "searchString", required = false) String searchString
  ) {
    TelegramUserData telegramUserData =
        (TelegramUserData) request.getAttribute(TelegramRequestAttributes.TELEGRAM_USER_DATA);
    return service.searchSelect(supplierId, telegramUserData, searchString);
  }
}
