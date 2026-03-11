package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Заказ поставщику со всеми позициями.
 */
@Value
@Builder
@Jacksonized
public class SupplierOrderUnprocessedResponseDto {

  @JsonProperty("id")
  UUID id;

  @JsonProperty("number")
  String number;

  @JsonProperty("customer_id")
  UUID customerId;

  @JsonProperty("supplier_id")
  UUID supplierId;

  @JsonProperty("user_id")
  UUID userId;

  @JsonProperty("datetime")
  LocalDateTime datetime;

  @JsonProperty("status")
  Boolean status;

  @JsonProperty("items")
  List<OrderItemResponseDto> items;

  @Value
  @Builder
  @Jacksonized
  public static class OrderItemResponseDto {

    @JsonProperty("id")
    UUID id;

    @JsonProperty("order_id")
    UUID orderId;

    @JsonProperty("nomenclature_id")
    UUID nomenclatureId;

    @JsonProperty("count")
    Integer count;

    @JsonProperty("price")
    Double price;

    @JsonProperty("sum")
    Double sum;
  }
}
