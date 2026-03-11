package ru.cource.priceorders.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * Ответ загрузки клиентов и адресов.
 * Возвращает созданные/обновлённые сущности (uuid) для последующего ручного
 * создания пользователей и связей.
 */
@Value
@Builder
@Jacksonized
public class CustomerUploadResponseDto {

  @JsonProperty("created_customers")
  int createdCustomers;

  @JsonProperty("updated_customers")
  int updatedCustomers;

  @JsonProperty("created_addresses")
  int createdAddresses;

  @JsonProperty("created_customer_address_links")
  int createdCustomerAddressLinks;

  @JsonProperty("customers")
  List<CustomerResultDto> customers;

  @Value
  @Builder
  @Jacksonized
  public static class CustomerResultDto {
    @JsonProperty("id")
    UUID id;

    @JsonProperty("system_guid")
    UUID systemGuid;

    @JsonProperty("name")
    String name;

    @JsonProperty("inn")
    String inn;

    @JsonProperty("kpp")
    String kpp;

    @JsonProperty("addresses")
    List<AddressResultDto> addresses;
  }

  @Value
  @Builder
  @Jacksonized
  public static class AddressResultDto {
    @JsonProperty("id")
    UUID id;

    @JsonProperty("address")
    String address;
  }
}
