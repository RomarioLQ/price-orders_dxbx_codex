package ru.cource.priceorders.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.models.dto.PriceListUploadRequestDto;
import ru.cource.priceorders.models.dto.PriceListUploadResponseDto;
import ru.cource.priceorders.service.PriceListUploadService;

/**
 * Integration API (1C): загрузка прайс-листа поставщика.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-lists")
public class PriceListUploadIntegrationController {

  private final PriceListUploadService service;

  @PostMapping("/import")
  public PriceListUploadResponseDto upload(@RequestBody @NotNull PriceListUploadRequestDto request) {
    return service.upload(request);
  }
}
