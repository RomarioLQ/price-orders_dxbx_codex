package ru.cource.priceorders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.AddressRepository;
import ru.cource.priceorders.dao.CustomerRepository;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.Address;
import ru.cource.priceorders.models.Customer;
import ru.cource.priceorders.models.dto.CustomerUploadRequestDto;
import ru.cource.priceorders.models.dto.CustomerUploadResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Загрузка клиентов и их адресов торговых точек в БД.
 * Используется интеграцией (обработка 1С поставщика).
 */
@Service
@RequiredArgsConstructor
public class CustomerUploadService {

  private final CustomerRepository customerRepository;
  private final AddressRepository addressRepository;
  private final TraceIdGenerator traceIdGenerator;

  @Transactional
  public CustomerUploadResponseDto upload(CustomerUploadRequestDto request) {
    List<CustomerUploadRequestDto.CustomerDto> customers = Optional.ofNullable(request)
        .map(CustomerUploadRequestDto::getCustomers)
        .filter(list -> !list.isEmpty())
        .orElseThrow(() -> validation("customers", "customers is required"));

    AtomicInteger createdCustomers = new AtomicInteger(0);
    AtomicInteger updatedCustomers = new AtomicInteger(0);
    AtomicInteger createdAddresses = new AtomicInteger(0);

    Set<UUID> requestGuids = new HashSet<>();

    List<CustomerUploadResponseDto.CustomerResultDto> resultCustomers = customers.stream()
        .map(c -> {
          if (c == null) {
            throw validation("customers", "customer item is null");
          }

          String inn = Optional.ofNullable(c.getInn()).orElse("").trim();
          String kpp = Optional.ofNullable(c.getKpp())
              .map(String::trim)
              .filter(s -> !s.isBlank())
              .orElse(null);
          String name = Optional.ofNullable(c.getName()).orElse("").trim();
          UUID systemGuid = Optional.ofNullable(c.getSystemGuid())
              .orElseThrow(() -> validation("customers.system_guid", "system_guid is required"));

          if (name.isBlank()) {
            throw validation("customers.name", "name is required");
          }
          if (inn.isBlank()) {
            throw validation("customers.inn", "inn is required");
          }

          if (!requestGuids.add(systemGuid)) {
            throw new ValidationException(
                "CUSTOMER_GUID_ALREADY_EXISTS",
                traceIdGenerator.gen(),
                "Customer with provided system_guid already exists",
                null,
                List.of(ParamDto.builder().key("system_guid").value(systemGuid.toString()).build())
            );
          }

          if (customerRepository.existsBySystemGuid(systemGuid)) {
            throw new ValidationException(
                "CUSTOMER_GUID_ALREADY_EXISTS",
                traceIdGenerator.gen(),
                "Customer with provided system_guid already exists",
                null,
                List.of(ParamDto.builder().key("system_guid").value(systemGuid.toString()).build())
            );
          }

          Customer customer = new Customer();
          customer.setId(UUID.randomUUID());
          customer.setSystemGuid(systemGuid);
          customer.setName(name);
          customer.setInn(inn);
          customer.setKpp(kpp);
          createdCustomers.incrementAndGet();

          Customer savedCustomer = customerRepository.save(customer);

          List<CustomerUploadRequestDto.AddressDto> addressDtos =
              Optional.ofNullable(c.getAddresses()).orElse(List.of());

          List<CustomerUploadResponseDto.AddressResultDto> resultAddresses = addressDtos.stream()
              .filter(Objects::nonNull)
              .map(a -> Optional.ofNullable(a.getAddress()).orElse("").trim())
              .filter(addressText -> !addressText.isBlank())
              .distinct()
              .map(addressText -> {
                Address created = new Address();
                created.setId(UUID.randomUUID());
                created.setCustomerId(savedCustomer.getId());
                created.setName(addressText);
                Address savedAddress = addressRepository.save(created);
                createdAddresses.incrementAndGet();

                return CustomerUploadResponseDto.AddressResultDto.builder()
                    .id(savedAddress.getId())
                    .address(savedAddress.getName())
                    .build();
              })
              .toList();

          return CustomerUploadResponseDto.CustomerResultDto.builder()
              .id(savedCustomer.getId())
              .systemGuid(savedCustomer.getSystemGuid())
              .name(savedCustomer.getName())
              .inn(savedCustomer.getInn())
              .kpp(savedCustomer.getKpp())
              .addresses(resultAddresses)
              .build();
        })
        .toList();

    return CustomerUploadResponseDto.builder()
        .createdCustomers(createdCustomers.get())
        .updatedCustomers(updatedCustomers.get())
        .createdAddresses(createdAddresses.get())
        .createdCustomerAddressLinks(0)
        .customers(resultCustomers)
        .build();
  }

  private ValidationException validation(String field, String message) {
    return new ValidationException(
        "PARAMS_VALIDATION_FAILED",
        traceIdGenerator.gen(),
        message,
        null,
        List.of(ParamDto.builder().key("field").value(field).build())
    );
  }
}
