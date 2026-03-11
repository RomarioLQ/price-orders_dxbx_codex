package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Запрос на подтверждение успешной выгрузки заказа...
 */
@Value
@Builder
@Jacksonized
public class OrderMarkUploadedRequestDto {

  @JsonProperty("order_id")
  UUID orderId;
}
