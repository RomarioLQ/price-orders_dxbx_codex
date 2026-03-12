package ru.cource.priceorders.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cource.priceorders.models.dto.CustomerUploadRequestDto;
import ru.cource.priceorders.models.dto.CustomerUploadResponseDto;
import ru.cource.priceorders.service.CustomerUploadService;

import java.util.List;
import java.util.UUID;

/**
 * Integration API (1C): загрузка внешних идентификаторов клиентов поставщика.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer-external-ids")
public class CustomerUploadIntegrationController {

  private final CustomerUploadService service;

  @PostMapping()
  public CustomerUploadResponseDto upload(
      @RequestHeader("secretWord") @NotNull UUID supplierId,
      @RequestBody @NotNull List<CustomerUploadRequestDto> request
  ) {
    return service.upload(supplierId, request);
  }
}
