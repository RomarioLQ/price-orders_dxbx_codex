package ru.cource.priceorders.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.models.dto.OrderMarkUploadedRequestDto;
import ru.cource.priceorders.models.dto.OrderMarkUploadedResponseDto;
import ru.cource.priceorders.models.dto.SupplierOrderUnprocessedResponseDto;
import ru.cource.priceorders.service.SupplierOrderIntegrationService;

import java.util.List;
import java.util.UUID;

/**
 * Integration API для поставщика (обработка 1С):
 * - получение невыгруженных заказов
 * - подтверждение успешной выгрузки
 */
@RestController
@RequestMapping("/integration/price-orders/v1/orders")
@RequiredArgsConstructor
@Validated
public class SupplierOrderIntegrationController {

  private final SupplierOrderIntegrationService service;

  @GetMapping("/unprocessed")
  public List<SupplierOrderUnprocessedResponseDto> getUnprocessed(
      @RequestParam(value = "supplierId", required = false) UUID supplierId,
      @RequestParam(value = "supplier_id", required = false) UUID supplierIdSnake
  ) {
    return service.getUnprocessed(java.util.Optional.ofNullable(supplierId).orElse(supplierIdSnake));
  }

  @PostMapping("/mark-uploaded")
  public OrderMarkUploadedResponseDto markUploaded(@RequestBody OrderMarkUploadedRequestDto request) {
    return service.markUploaded(request);
  }
}
