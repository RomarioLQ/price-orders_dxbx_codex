package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Запрос на загрузку прайс-листа и его позиций.
 */
@Value
@Builder
@Jacksonized
public class PriceListUploadRequestDto {

  @JsonProperty("supplier")
  SupplierRefDto supplier;

  @JsonProperty("customer_id")
  UUID customerExternalId;

  @JsonProperty("price_list")
  PriceListDto priceList;

  @JsonProperty("positions")
  List<PriceListPositionDto> positions;

  @Value
  @Builder
  @Jacksonized
  public static class SupplierRefDto {
    @JsonProperty("id")
    UUID id;
  }

  @Value
  @Builder
  @Jacksonized
  public static class PriceListDto {

    @JsonProperty("start_date")
    LocalDateTime startDate;

    @JsonProperty("end_date")
    LocalDateTime endDate;
  }

  @Value
  @Builder
  @Jacksonized
  public static class PriceListPositionDto {
    @JsonProperty("nomenclature_id")
    UUID nomenclatureId;

    @JsonProperty("code")
    String code;

    @JsonProperty("name")
    String name;

    @JsonProperty("unit_measure")
    String unitMeasure;

    @JsonProperty("price_with_vat")
    BigDecimal priceWithVat;

    @JsonProperty("vat")
    Long vat;
  }
}
