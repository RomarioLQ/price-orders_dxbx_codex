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
import ru.cource.priceorders.models.dto.SupplierSearchSelectResponseDto;
import ru.cource.priceorders.service.SupplierSearchSelectService;

import java.util.List;

/**
 * Front API для динамического поиска поставщиков (search-select).
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Validated
public class SupplierSearchSelectFrontController {

  private final SupplierSearchSelectService service;

  @GetMapping("/search-select")
  public List<SupplierSearchSelectResponseDto> searchSelect(
      HttpServletRequest request,
      @RequestParam(value = "searchString", required = false) String searchString
  ) {
    TelegramUserData telegramUserData =
        (TelegramUserData) request.getAttribute(TelegramRequestAttributes.TELEGRAM_USER_DATA);
    return service.searchSelect(telegramUserData, searchString);
  }
}
