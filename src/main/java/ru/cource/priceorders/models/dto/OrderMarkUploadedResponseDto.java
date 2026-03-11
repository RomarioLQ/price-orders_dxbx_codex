package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Ответ на подтверждение выгрузки.
 */
@Value
@Builder
@Jacksonized
public class OrderMarkUploadedResponseDto {

  @JsonProperty("order_id")
  UUID orderId;

  @JsonProperty("status")
  Boolean status;
}
