package ru.cource.priceorders.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.filter.TelegramRequestAttributes;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.dto.OrderUploadRequestDto;
import ru.cource.priceorders.models.dto.OrderUploadResponseDto;
import ru.cource.priceorders.service.OrderUploadService;

/**
 * Front API для отправки заказа из Telegram Web App.
 */
@RestController
@RequestMapping("/front/price-orders/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderUploadFrontController {

  private final OrderUploadService service;

  @PostMapping("/upload")
  public OrderUploadResponseDto upload(HttpServletRequest servletRequest, @RequestBody OrderUploadRequestDto request) {
    TelegramUserData telegramUserData =
        (TelegramUserData) servletRequest.getAttribute(TelegramRequestAttributes.TELEGRAM_USER_DATA);
    return service.upload(request, telegramUserData);
  }
}
