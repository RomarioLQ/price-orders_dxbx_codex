package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Ответ на загрузку прайс-листа.
 */
@Value
@Builder
@Jacksonized
public class PriceListUploadResponseDto {

  @JsonProperty("price_id")
  UUID priceId;

  @JsonProperty("created_nomenclature")
  Integer createdNomenclature;

  @JsonProperty("positions")
  Integer positions;
}
