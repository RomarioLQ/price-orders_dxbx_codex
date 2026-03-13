package ru.cource.priceorders.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cource.priceorders.dao.OrderItemRepository;
import ru.cource.priceorders.dao.SystemOrderRepository;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.OrderItem;
import ru.cource.priceorders.models.SystemOrder;
import ru.cource.priceorders.models.dto.OrderMarkUploadedRequestDto;
import ru.cource.priceorders.models.dto.OrderMarkUploadedResponseDto;
import ru.cource.priceorders.models.dto.SupplierOrderUnprocessedResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierOrderIntegrationService {

  private final SystemOrderRepository systemOrderRepository;
  private final OrderItemRepository orderItemRepository;
  private final TraceIdGenerator traceIdGenerator;

  @Transactional(readOnly = true)
  public List<SupplierOrderUnprocessedResponseDto> getUnprocessed(UUID supplierId) {
    UUID sid = Optional.ofNullable(supplierId)
        .orElseThrow(() -> validation("supplierId", "supplierId is required"));

    List<SystemOrder> orders = systemOrderRepository.findAllBySupplierIdAndStatusFalse(sid);
    if (orders.isEmpty()) {
      return List.of();
    }

    List<UUID> orderIds = orders.stream().map(SystemOrder::getId).toList();
    Map<UUID, List<OrderItem>> itemsByOrderId = orderItemRepository.findAllByOrderIdIn(orderIds).stream()
        .collect(Collectors.groupingBy(OrderItem::getOrderId));

    return orders.stream()
        .map(o -> SupplierOrderUnprocessedResponseDto.builder()
            .id(o.getId())
            .number(o.getNumber())
            .customerId(o.getCustomerId())
            .supplierId(o.getSupplierId())
            .addressId(o.getAddressId())
            .userId(o.getUserId())
            .datetime(o.getDatetime())
            .status(o.getStatus())
            .items(Optional.ofNullable(itemsByOrderId.get(o.getId())).orElse(List.of()).stream()
                .map(toItemDto())
                .toList())
            .build())
        .toList();
  }

  @Transactional
  public OrderMarkUploadedResponseDto markUploaded(OrderMarkUploadedRequestDto request) {
    if (request == null || request.getOrderId() == null) {
      throw validation("order_id", "order_id is required");
    }

    UUID orderId = request.getOrderId();
    SystemOrder order = systemOrderRepository.findById(orderId)
        .orElseThrow(() -> new NotFoundException(
            "ORDER_NOT_FOUND",
            traceIdGenerator.gen(),
            "Order is not found",
            null,
            List.of(ParamDto.builder().key("orderId").value(orderId.toString()).build())
        ));

    order.setStatus(Boolean.TRUE);
    systemOrderRepository.save(order);

    return OrderMarkUploadedResponseDto.builder()
        .orderId(orderId)
        .status(Boolean.TRUE)
        .build();
  }

  private Function<OrderItem, SupplierOrderUnprocessedResponseDto.OrderItemResponseDto> toItemDto() {
    return i -> SupplierOrderUnprocessedResponseDto.OrderItemResponseDto.builder()
        .id(i.getId())
        .orderId(i.getOrderId())
        .nomenclatureId(i.getNomenclatureId())
        .count(i.getCount())
        .price(i.getPrice())
        .sum(i.getSum())
        .build();
  }

  private ValidationException validation(String key, String message) {
    return new ValidationException(
        "PARAMS_VALIDATION_FAILED",
        traceIdGenerator.gen(),
        message,
        null,
        List.of(ParamDto.builder().key(key).value(message).build())
    );
  }
}
