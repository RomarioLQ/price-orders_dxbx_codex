package ru.cource.priceorders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.CustomerExternalIdRepository;
import ru.cource.priceorders.dao.CustomerRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.Customer;
import ru.cource.priceorders.models.CustomerExternalId;
import ru.cource.priceorders.models.dto.CustomerUploadRequestDto;
import ru.cource.priceorders.models.dto.CustomerUploadResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сопоставление клиентов поставщика с внутренними customer по inn/kpp.
 */
@Service
@RequiredArgsConstructor
public class CustomerUploadService {

  private static final String STATUS_CREATED = "CREATED";
  private static final String STATUS_SKIPPED = "SKIPPED_ALREADY_EXISTS";

  private final CustomerRepository customerRepository;
  private final SupplierRepository supplierRepository;
  private final CustomerExternalIdRepository customerExternalIdRepository;
  private final TraceIdGenerator traceIdGenerator;

  @Transactional
  public CustomerUploadResponseDto upload(UUID supplierId, List<CustomerUploadRequestDto> request) {
    if (!supplierRepository.existsById(supplierId)) {
      throw validation("secretWord", "Supplier not found");
    }

    List<CustomerUploadRequestDto> items = Optional.ofNullable(request)
        .filter(list -> !list.isEmpty())
        .orElseThrow(() -> validation("request", "request body is required"));

    int matched = 0;
    int created = 0;
    int skipped = 0;

    List<CustomerUploadResponseDto.ProcessedItemDto> processed = new ArrayList<>();
    List<CustomerUploadResponseDto.NotMatchedItemDto> notMatched = new ArrayList<>();

    for (CustomerUploadRequestDto rawItem : items) {
      CustomerUploadRequestDto item = validateItem(rawItem);
      Optional<Customer> customer = findCustomer(item);

      if (customer.isEmpty()) {
        notMatched.add(CustomerUploadResponseDto.NotMatchedItemDto.builder()
            .customerExternalId(item.getCustomerExternalId())
            .inn(item.getInn())
            .kpp(item.getKpp())
            .reason("CUSTOMER_NOT_FOUND_BY_INN_KPP")
            .build());
        continue;
      }

      matched++;

      UUID customerId = customer.get().getId();
      boolean exists = customerExternalIdRepository.existsByCustomerIdAndSupplierId(customerId, supplierId);

      String status;
      if (exists) {
        status = STATUS_SKIPPED;
        skipped++;
      } else {
        CustomerExternalId mapping = new CustomerExternalId();
        mapping.setId(UUID.randomUUID());
        mapping.setCustomerId(customerId);
        mapping.setSupplierId(supplierId);
        mapping.setCustomerExternalId(item.getCustomerExternalId());
        customerExternalIdRepository.save(mapping);
        status = STATUS_CREATED;
        created++;
      }

      processed.add(CustomerUploadResponseDto.ProcessedItemDto.builder()
          .customerId(customerId)
          .supplierId(supplierId)
          .customerExternalId(item.getCustomerExternalId())
          .inn(item.getInn())
          .kpp(item.getKpp())
          .status(status)
          .build());
    }

    return CustomerUploadResponseDto.builder()
        .total(items.size())
        .matched(matched)
        .created(created)
        .skipped(skipped)
        .notFound(notMatched.size())
        .processed(processed)
        .notMatched(notMatched)
        .build();
  }

  private Optional<Customer> findCustomer(CustomerUploadRequestDto item) {
    return customerRepository.findFirstByInnAndKpp(item.getInn(), item.getKpp());
  }

  private CustomerUploadRequestDto validateItem(CustomerUploadRequestDto item) {
    CustomerUploadRequestDto normalized = normalize(item);
    if (normalized.getCustomerExternalId() == null) {
      throw validation("id", "id is required");
    }
    if (normalized.getInn().isBlank()) {
      throw validation("inn", "inn is required");
    }
    return normalized;
  }

  private CustomerUploadRequestDto normalize(CustomerUploadRequestDto item) {
    if (item == null) {
      throw validation("request", "request item is null");
    }

    return CustomerUploadRequestDto.builder()
        .customerExternalId(item.getCustomerExternalId())
        .inn(Optional.ofNullable(item.getInn()).orElse("").trim())
        .kpp(Optional.ofNullable(item.getKpp())
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .orElse(null))
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
