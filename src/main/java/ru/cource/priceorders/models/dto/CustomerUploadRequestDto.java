package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Элемент запроса на сопоставление клиента поставщика с внутренним customer.
 */
@Value
@Builder
@Jacksonized
public class CustomerUploadRequestDto {

  /**
   * GUID клиента в учетной системе поставщика.
   */
  @JsonProperty("id")
  UUID customerExternalId;

  @JsonProperty("inn")
  String inn;

  @JsonProperty("kpp")
  String kpp;
}
