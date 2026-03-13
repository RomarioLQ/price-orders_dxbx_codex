package ru.cource.priceorders.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cource.priceorders.dao.OrderItemRepository;
import ru.cource.priceorders.dao.SystemOrderRepository;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.OrderItem;
import ru.cource.priceorders.models.SystemOrder;
import ru.cource.priceorders.models.dto.OrderMarkUploadedRequestDto;
import ru.cource.priceorders.models.dto.SupplierOrderUnprocessedResponseDto;

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
class SupplierOrderIntegrationServiceTest {

  @Mock
  private SystemOrderRepository systemOrderRepository;
  @Mock
  private OrderItemRepository orderItemRepository;
  @Mock
  private TraceIdGenerator traceIdGenerator;

  @InjectMocks
  private SupplierOrderIntegrationService supplierOrderIntegrationService;

  @Test
  void getUnprocessedMapsOrderAndItems() {
    UUID supplierId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID nomenclatureId = UUID.randomUUID();

    SystemOrder order = new SystemOrder();
    order.setId(orderId);
    order.setSupplierId(supplierId);
    order.setDatetime(LocalDateTime.of(2026, 3, 12, 9, 0));

    OrderItem item = new OrderItem();
    item.setId(UUID.randomUUID());
    item.setOrderId(orderId);
    item.setNomenclatureId(nomenclatureId);
    item.setCount(3);
    item.setPrice(10d);
    item.setSum(30d);

    when(systemOrderRepository.findAllUnprocessedBySupplierId(supplierId)).thenReturn(List.of(order));
    when(orderItemRepository.findAllByOrderIdIn(List.of(orderId))).thenReturn(List.of(item));

    List<SupplierOrderUnprocessedResponseDto> response = supplierOrderIntegrationService.getUnprocessed(supplierId);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).getId()).isEqualTo(orderId);
    assertThat(response.get(0).getItems()).hasSize(1);
    assertThat(response.get(0).getItems().get(0).getNomenclatureId()).isEqualTo(nomenclatureId);
  }

  @Test
  void markUploadedThrowsNotFoundWhenOrderDoesNotExist() {
    UUID orderId = UUID.randomUUID();
    when(traceIdGenerator.gen()).thenReturn("price-orders-1");
    when(systemOrderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> supplierOrderIntegrationService.markUploaded(
        OrderMarkUploadedRequestDto.builder().orderId(orderId).build()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Order is not found");
  }

  @Test
  void markUploadedMarksOrderAsProcessed() {
    UUID orderId = UUID.randomUUID();
    SystemOrder order = new SystemOrder();
    order.setId(orderId);
    order.setStatus(Boolean.FALSE);

    when(systemOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

    supplierOrderIntegrationService.markUploaded(OrderMarkUploadedRequestDto.builder().orderId(orderId).build());

    assertThat(order.getStatus()).isTrue();
    verify(systemOrderRepository).save(any(SystemOrder.class));
  }
}
