package ru.cource.priceorders.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cource.priceorders.dao.CustomerRepository;
import ru.cource.priceorders.dao.NomenclatureRepository;
import ru.cource.priceorders.dao.PriceListRepository;
import ru.cource.priceorders.dao.PricePositionRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.Customer;
import ru.cource.priceorders.models.dto.PriceListUploadRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceListUploadServiceTest {

  @Mock
  private SupplierRepository supplierRepository;
  @Mock
  private CustomerRepository customerRepository;
  @Mock
  private NomenclatureRepository nomenclatureRepository;
  @Mock
  private PriceListRepository priceListRepository;
  @Mock
  private PricePositionRepository pricePositionRepository;
  @Mock
  private TraceIdGenerator traceIdGenerator;

  @InjectMocks
  private PriceListUploadService priceListUploadService;

  @Test
  void uploadThrowsNotFoundWhenCustomerIdNotExistsInCurrentSchema() {
    UUID supplierId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    when(supplierRepository.existsById(supplierId)).thenReturn(true);
    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
    when(traceIdGenerator.gen()).thenReturn("price-orders-000001");

    PriceListUploadRequestDto request = PriceListUploadRequestDto.builder()
        .supplier(PriceListUploadRequestDto.SupplierRefDto.builder().id(supplierId).build())
        .priceList(PriceListUploadRequestDto.PriceListDto.builder()
            .startDate(LocalDateTime.now())
            .isActive(true)
            .customerSystemGuid(customerId)
            .build())
        .positions(List.of(PriceListUploadRequestDto.PriceListPositionDto.builder()
            .code("A")
            .name("B")
            .priceWithVat(java.math.BigDecimal.ONE)
            .vat(20L)
            .build()))
        .build();

    assertThatThrownBy(() -> priceListUploadService.upload(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Customer is not found");
  }
}
