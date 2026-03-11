package ru.cource.priceorders.models.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Ответ search-select по поставщикам.
 */
@Value
@Builder
public class SupplierSearchSelectResponseDto {

  UUID id;
  String name;
}
