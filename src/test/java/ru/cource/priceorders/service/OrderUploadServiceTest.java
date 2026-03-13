package ru.cource.priceorders.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cource.priceorders.dao.AddressRepository;
import ru.cource.priceorders.dao.NomenclatureRepository;
import ru.cource.priceorders.dao.OrderItemRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.dao.SystemOrderRepository;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.Address;
import ru.cource.priceorders.models.Nomenclature;
import ru.cource.priceorders.models.OrderItem;
import ru.cource.priceorders.models.SystemOrder;
import ru.cource.priceorders.models.dto.OrderUploadRequestDto;
import ru.cource.priceorders.models.dto.OrderUploadResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderUploadServiceTest {

  @Mock
  private SupplierRepository supplierRepository;
  @Mock
  private TelegramUserContextResolverService telegramUserContextResolverService;
  @Mock
  private SystemOrderRepository systemOrderRepository;
  @Mock
  private OrderItemRepository orderItemRepository;
  @Mock
  private NomenclatureRepository nomenclatureRepository;
  @Mock
  private AddressRepository addressRepository;
  @Mock
  private TraceIdGenerator traceIdGenerator;

  @InjectMocks
  private OrderUploadService orderUploadService;

  @Test
  void uploadCreatesOrderAndUsesDatetimeFromRequest() {
    UUID userId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    UUID supplierId = UUID.randomUUID();
    UUID addressId = UUID.randomUUID();
    UUID nomenclatureId = UUID.randomUUID();
    LocalDateTime datetime = LocalDateTime.of(2026, 3, 11, 10, 30);

    when(telegramUserContextResolverService.resolve(any(TelegramUserData.class)))
        .thenReturn(new TelegramUserContextResolverService.TelegramUserContext(userId, customerId));
    when(supplierRepository.existsById(supplierId)).thenReturn(true);

    Address address = new Address();
    address.setId(addressId);
    address.setCustomerId(customerId);
    when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

    Nomenclature nomenclature = new Nomenclature();
    nomenclature.setId(nomenclatureId);
    nomenclature.setSupplierId(supplierId);
    when(nomenclatureRepository.findById(nomenclatureId)).thenReturn(Optional.of(nomenclature));

    OrderUploadRequestDto request = OrderUploadRequestDto.builder()
        .supplierId(supplierId)
        .addressId(addressId)
        .datetime(datetime)
        .items(List.of(OrderUploadRequestDto.OrderItemDto.builder()
            .nomenclatureId(nomenclatureId)
            .count(2)
            .price(10.5)
            .sum(21.0)
            .build()))
        .build();

    OrderUploadResponseDto response = orderUploadService.upload(request, new TelegramUserData("raw", "123"));

    assertThat(response.getItems()).isEqualTo(1);
    verify(systemOrderRepository).save(any(SystemOrder.class));
    verify(orderItemRepository).saveAll(any(List.class));
  }

  @Test
  void uploadThrowsValidationWhenAddressDoesNotBelongToCurrentCustomer() {
    UUID userId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    UUID anotherCustomerId = UUID.randomUUID();
    UUID supplierId = UUID.randomUUID();
    UUID addressId = UUID.randomUUID();
    UUID nomenclatureId = UUID.randomUUID();

    when(traceIdGenerator.gen()).thenReturn("price-orders-000001");
    when(telegramUserContextResolverService.resolve(any(TelegramUserData.class)))
        .thenReturn(new TelegramUserContextResolverService.TelegramUserContext(userId, customerId));
    when(supplierRepository.existsById(supplierId)).thenReturn(true);

    Address address = new Address();
    address.setId(addressId);
    address.setCustomerId(anotherCustomerId);
    when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

    Nomenclature nomenclature = new Nomenclature();
    nomenclature.setId(nomenclatureId);
    nomenclature.setSupplierId(supplierId);
    when(nomenclatureRepository.findById(nomenclatureId)).thenReturn(Optional.of(nomenclature));

    OrderUploadRequestDto request = OrderUploadRequestDto.builder()
        .supplierId(supplierId)
        .addressId(addressId)
        .datetime(LocalDateTime.now())
        .items(List.of(OrderUploadRequestDto.OrderItemDto.builder()
            .nomenclatureId(nomenclatureId)
            .count(1)
            .price(1.0)
            .sum(1.0)
            .build()))
        .build();

    assertThatThrownBy(() -> orderUploadService.upload(request, new TelegramUserData("raw", "123")))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Address is not available for current customer");
  }
}
