package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * Запрос на загрузку клиентов (customer) и их адресов торговых точек (address).
 * Используется интеграцией (обработка 1С).
 */
@Value
@Builder
@Jacksonized
public class CustomerUploadRequestDto {

  @JsonProperty("customers")
  List<CustomerDto> customers;

  @Value
  @Builder
  @Jacksonized
  public static class CustomerDto {

    /**
     * GUID клиента в учетной системе поставщика.
     */
    @JsonProperty("system_guid")
    UUID systemGuid;

    @JsonProperty("name")
    String name;

    @JsonProperty("inn")
    String inn;

    @JsonProperty("kpp")
    String kpp;

    /**
     * Адреса торговых точек клиента.
     */
    @JsonProperty("addresses")
    List<AddressDto> addresses;
  }

  @Value
  @Builder
  @Jacksonized
  public static class AddressDto {
    @JsonProperty("address")
    String address;
  }
}
