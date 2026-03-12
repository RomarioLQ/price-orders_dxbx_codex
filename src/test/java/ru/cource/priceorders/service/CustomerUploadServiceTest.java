package ru.cource.priceorders.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cource.priceorders.dao.CustomerExternalIdRepository;
import ru.cource.priceorders.dao.CustomerRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.Customer;
import ru.cource.priceorders.models.CustomerExternalId;
import ru.cource.priceorders.models.dto.CustomerUploadRequestDto;
import ru.cource.priceorders.models.dto.CustomerUploadResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUploadServiceTest {

  @Mock
  private CustomerRepository customerRepository;
  @Mock
  private SupplierRepository supplierRepository;
  @Mock
  private CustomerExternalIdRepository customerExternalIdRepository;
  @Mock
  private TraceIdGenerator traceIdGenerator;

  @InjectMocks
  private CustomerUploadService customerUploadService;

  @Test
  void uploadCreatesMappingWhenCustomerMatchedByInnAndKpp() {
    UUID supplierId = UUID.fromString("3f95cf43-e820-2599-3da5-f40fb70da0ab");
    UUID customerId = UUID.fromString("66666666-2222-3333-4444-555555555555");
    UUID externalId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    Customer customer = new Customer();
    customer.setId(customerId);
    customer.setInn("7700000001");
    customer.setKpp("770001001");

    when(supplierRepository.existsById(supplierId)).thenReturn(true);
    when(customerRepository.findFirstByInnAndKpp("7700000001", "770001001")).thenReturn(Optional.of(customer));
    when(customerExternalIdRepository.findFirstByCustomerIdAndSupplierId(customerId, supplierId)).thenReturn(Optional.empty());

    CustomerUploadResponseDto response = customerUploadService.upload(supplierId, List.of(
        CustomerUploadRequestDto.builder()
            .customerExternalId(externalId)
            .inn("7700000001")
            .kpp("770001001")
            .build()
    ));

    assertThat(response.getTotal()).isEqualTo(1);
    assertThat(response.getMatched()).isEqualTo(1);
    assertThat(response.getCreated()).isEqualTo(1);
    assertThat(response.getSkipped()).isEqualTo(0);
    assertThat(response.getNotFound()).isEqualTo(0);
    assertThat(response.getProcessed()).hasSize(1);
    assertThat(response.getProcessed().getFirst().getStatus()).isEqualTo("CREATED");

    verify(customerExternalIdRepository).save(any(CustomerExternalId.class));
  }

  @Test
  void uploadReturnsNotMatchedWhenCustomerMissing() {
    UUID supplierId = UUID.fromString("3f95cf43-e820-2599-3da5-f40fb70da0ab");
    UUID externalId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    when(supplierRepository.existsById(supplierId)).thenReturn(true);
    when(customerRepository.findFirstByInnAndKpp("7700000001", "770001001")).thenReturn(Optional.empty());

    CustomerUploadResponseDto response = customerUploadService.upload(supplierId, List.of(
        CustomerUploadRequestDto.builder()
            .customerExternalId(externalId)
            .inn("7700000001")
            .kpp("770001001")
            .build()
    ));

    assertThat(response.getTotal()).isEqualTo(1);
    assertThat(response.getMatched()).isEqualTo(0);
    assertThat(response.getCreated()).isEqualTo(0);
    assertThat(response.getSkipped()).isEqualTo(0);
    assertThat(response.getNotFound()).isEqualTo(1);
    assertThat(response.getNotMatched()).hasSize(1);
    assertThat(response.getNotMatched().getFirst().getReason()).isEqualTo("CUSTOMER_NOT_FOUND_BY_INN_KPP");

    verify(customerExternalIdRepository, never()).save(any(CustomerExternalId.class));
  }


  @Test
  void uploadUpdatesExternalIdWhenMappingAlreadyExists() {
    UUID supplierId = UUID.fromString("3f95cf43-e820-2599-3da5-f40fb70da0ab");
    UUID customerId = UUID.fromString("66666666-2222-3333-4444-555555555555");
    UUID oldExternalId = UUID.fromString("aaaaaaaa-2222-3333-4444-555555555555");
    UUID newExternalId = UUID.fromString("bbbbbbbb-2222-3333-4444-555555555555");

    Customer customer = new Customer();
    customer.setId(customerId);
    customer.setInn("7700000001");
    customer.setKpp("770001001");

    CustomerExternalId mapping = new CustomerExternalId();
    mapping.setId(UUID.randomUUID());
    mapping.setCustomerId(customerId);
    mapping.setSupplierId(supplierId);
    mapping.setCustomerExternalId(oldExternalId);

    when(supplierRepository.existsById(supplierId)).thenReturn(true);
    when(customerRepository.findFirstByInnAndKpp("7700000001", "770001001")).thenReturn(Optional.of(customer));
    when(customerExternalIdRepository.findFirstByCustomerIdAndSupplierId(customerId, supplierId))
        .thenReturn(Optional.of(mapping));

    CustomerUploadResponseDto response = customerUploadService.upload(supplierId, List.of(
        CustomerUploadRequestDto.builder()
            .customerExternalId(newExternalId)
            .inn("7700000001")
            .kpp("770001001")
            .build()
    ));

    assertThat(response.getCreated()).isEqualTo(0);
    assertThat(response.getSkipped()).isEqualTo(1);
    assertThat(response.getProcessed().getFirst().getStatus()).isEqualTo("UPDATED");

    verify(customerExternalIdRepository).save(argThat(saved -> newExternalId.equals(saved.getCustomerExternalId())));
  }

  @Test
  void uploadSkipsWhenExistingExternalIdEqualsIncoming() {
    UUID supplierId = UUID.fromString("3f95cf43-e820-2599-3da5-f40fb70da0ab");
    UUID customerId = UUID.fromString("66666666-2222-3333-4444-555555555555");
    UUID externalId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    Customer customer = new Customer();
    customer.setId(customerId);
    customer.setInn("7700000001");
    customer.setKpp("770001001");

    CustomerExternalId mapping = new CustomerExternalId();
    mapping.setId(UUID.randomUUID());
    mapping.setCustomerId(customerId);
    mapping.setSupplierId(supplierId);
    mapping.setCustomerExternalId(externalId);

    when(supplierRepository.existsById(supplierId)).thenReturn(true);
    when(customerRepository.findFirstByInnAndKpp("7700000001", "770001001")).thenReturn(Optional.of(customer));
    when(customerExternalIdRepository.findFirstByCustomerIdAndSupplierId(customerId, supplierId))
        .thenReturn(Optional.of(mapping));

    CustomerUploadResponseDto response = customerUploadService.upload(supplierId, List.of(
        CustomerUploadRequestDto.builder()
            .customerExternalId(externalId)
            .inn("7700000001")
            .kpp("770001001")
            .build()
    ));

    assertThat(response.getCreated()).isEqualTo(0);
    assertThat(response.getSkipped()).isEqualTo(1);
    assertThat(response.getProcessed().getFirst().getStatus()).isEqualTo("SKIPPED_ALREADY_EXISTS");

    verify(customerExternalIdRepository, never()).save(argThat(saved -> externalId.equals(saved.getCustomerExternalId()) && customerId.equals(saved.getCustomerId())));
  }

}
