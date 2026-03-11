package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Ответ на загрузку заказа.
 */
@Value
@Builder
@Jacksonized
public class OrderUploadResponseDto {

  @JsonProperty("order_id")
  UUID orderId;

  @JsonProperty("items")
  Integer items;
}
