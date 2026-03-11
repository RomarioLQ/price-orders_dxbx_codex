package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * Запрос на загрузку заказа (system_orders) и его позиций (order_item).
 */
@Value
@Builder
@Jacksonized
public class OrderUploadRequestDto {

  /**
   * id заказа (используется как номер документа). В БД system_orders.id генерируется автоматически,
   * а значение из этого поля сохраняется в system_orders.number.
   */
  @JsonProperty("id")
  String id;

  @JsonProperty("supplier_id")
  UUID supplierId;

  @JsonProperty("items")
  List<OrderItemDto> items;

  @Value
  @Builder
  @Jacksonized
  public static class OrderItemDto {
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
