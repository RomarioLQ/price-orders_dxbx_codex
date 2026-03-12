package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * Результат сопоставления внешних идентификаторов клиентов поставщика.
 */
@Value
@Builder
@Jacksonized
public class CustomerUploadResponseDto {

  @JsonProperty("total")
  int total;

  @JsonProperty("matched")
  int matched;

  @JsonProperty("created")
  int created;

  @JsonProperty("skipped")
  int skipped;

  @JsonProperty("not_found")
  int notFound;

  @JsonProperty("processed")
  List<ProcessedItemDto> processed;

  @JsonProperty("not_matched")
  List<NotMatchedItemDto> notMatched;

  @Value
  @Builder
  @Jacksonized
  public static class ProcessedItemDto {

    @JsonProperty("customer_id")
    UUID customerId;

    @JsonProperty("supplier_id")
    UUID supplierId;

    @JsonProperty("customer_external_id")
    UUID customerExternalId;

    @JsonProperty("inn")
    String inn;

    @JsonProperty("kpp")
    String kpp;

    @JsonProperty("status")
    String status;
  }

  @Value
  @Builder
  @Jacksonized
  public static class NotMatchedItemDto {

    @JsonProperty("customer_external_id")
    UUID customerExternalId;

    @JsonProperty("inn")
    String inn;

    @JsonProperty("kpp")
    String kpp;

    @JsonProperty("reason")
    String reason;
  }
}
