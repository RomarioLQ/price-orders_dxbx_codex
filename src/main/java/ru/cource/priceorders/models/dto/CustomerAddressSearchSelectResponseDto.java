package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * DTO ответа для search-select адресов клиента.
 */
@Value
@Builder
@Jacksonized
public class CustomerAddressSearchSelectResponseDto {

  @JsonProperty("id")
  UUID id;

  @JsonProperty("address")
  String address;
}
