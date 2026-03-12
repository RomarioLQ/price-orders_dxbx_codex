package ru.cource.priceorders.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.filter.TelegramRequestAttributes;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.dto.CustomerAddressSearchSelectResponseDto;
import ru.cource.priceorders.service.CustomerAddressSearchSelectService;

import java.util.List;

/**
 * Front API: search-select адресов клиента (по Telegram initData).
 */
@RestController
@RequestMapping("/front/price-orders/v1/addresses")
@RequiredArgsConstructor
@Validated
public class CustomerAddressSearchSelectFrontController {

  private final CustomerAddressSearchSelectService service;

  @GetMapping("/search-select")
  public List<CustomerAddressSearchSelectResponseDto> searchSelect(
      HttpServletRequest request,
      @RequestParam(value = "searchString", required = false) String searchString
  ) {
    TelegramUserData telegramUserData =
        (TelegramUserData) request.getAttribute(TelegramRequestAttributes.TELEGRAM_USER_DATA);
    return service.searchSelect(searchString, telegramUserData);
  }
}
