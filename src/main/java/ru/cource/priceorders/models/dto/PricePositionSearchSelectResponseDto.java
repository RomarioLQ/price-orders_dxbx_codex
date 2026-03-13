package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO ответа для search-select номенклатуры/позиций прайс-листа.
 */
@Value
@Builder
@Jacksonized
public class PricePositionSearchSelectResponseDto {

  /** ID выбранного прайс-листа (price_list.id). */
  @JsonProperty("price_id")
  UUID priceId;

  /** ID номенклатуры (nomenclature.id), также хранится в price_positions.nomenclature_id. */
  @JsonProperty("nomenclature_id")
  UUID nomenclatureId;

  /** Наименование номенклатуры (nomenclature.name). */
  @JsonProperty("name")
  String name;

  /** Единица измерения (price_positions.unit_measure). */
  @JsonProperty("unit_measure")
  String unitMeasure;

  /** Цена с НДС из прайс-листа (price_positions.price_with_vat). */
  @JsonProperty("price_with_vat")
  BigDecimal priceWithVat;
}
