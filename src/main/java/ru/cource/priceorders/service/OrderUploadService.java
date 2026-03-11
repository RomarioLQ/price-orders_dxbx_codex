package ru.cource.priceorders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.OrderItemRepository;
import ru.cource.priceorders.dao.NomenclatureRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.dao.SystemOrderRepository;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.OrderItem;
import ru.cource.priceorders.models.SystemOrder;
import ru.cource.priceorders.models.dto.OrderUploadRequestDto;
import ru.cource.priceorders.models.dto.OrderUploadResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderUploadService {

  private final SupplierRepository supplierRepository;
  private final TelegramUserContextResolverService telegramUserContextResolverService;
  private final SystemOrderRepository systemOrderRepository;
  private final OrderItemRepository orderItemRepository;
  private final NomenclatureRepository nomenclatureRepository;
  private final TraceIdGenerator traceIdGenerator;

  @Transactional
  public OrderUploadResponseDto upload(OrderUploadRequestDto request, TelegramUserData telegramUserData) {
    if (request == null) {
      throw validation("request", "request body is required");
    }

    String orderNumber = Optional.ofNullable(request.getId())
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .orElseThrow(() -> validation("id", "id is required"));
    UUID supplierId = Optional.ofNullable(request.getSupplierId())
        .orElseThrow(() -> validation("supplier_id", "supplier_id is required"));
    TelegramUserContextResolverService.TelegramUserContext telegramUserContext =
        telegramUserContextResolverService.resolve(telegramUserData);
    UUID userId = telegramUserContext.userId();
    UUID customerId = telegramUserContext.customerId();

    List<OrderUploadRequestDto.OrderItemDto> items = Optional.ofNullable(request.getItems()).orElse(List.of());
    if (items.isEmpty()) {
      throw validation("items", "items must not be empty");
    }

    if (!supplierRepository.existsById(supplierId)) {
      throw new NotFoundException(
          "SUPPLIER_NOT_FOUND",
          traceIdGenerator.gen(),
          "Supplier is not found",
          null,
          List.of(ParamDto.builder().key("supplierId").value(supplierId.toString()).build())
      );
    }

    if (systemOrderRepository.existsBySupplierIdAndNumber(supplierId, orderNumber)) {
      throw new ValidationException(
          "ORDER_ALREADY_EXISTS",
          traceIdGenerator.gen(),
          "Order with provided number already exists",
          null,
          List.of(ParamDto.builder().key("id").value(orderNumber).build())
      );
    }

    // Валидация номенклатур (без циклов)
    IntStream.range(0, items.size()).forEach(i -> {
      OrderUploadRequestDto.OrderItemDto it = items.get(i);
      UUID nomenclatureId = it == null ? null : it.getNomenclatureId();
      if (nomenclatureId == null) {
        throw validation("items[" + i + "].nomenclature_id", "nomenclature_id is required");
      }
      if (!nomenclatureRepository.existsById(nomenclatureId)) {
        throw new NotFoundException(
            "NOMENCLATURE_NOT_FOUND",
            traceIdGenerator.gen(),
            "Nomenclature is not found",
            null,
            List.of(
                ParamDto.builder().key("nomenclatureId").value(nomenclatureId.toString()).build(),
                ParamDto.builder().key("itemIndex").value(String.valueOf(i)).build()
            )
        );
      }
    });

    SystemOrder order = new SystemOrder();
    UUID orderId = UUID.randomUUID();
    order.setId(orderId);
    order.setCustomerId(customerId);
    order.setSupplierId(supplierId);
    order.setUserId(userId);
    order.setDatetime(LocalDateTime.now());
    order.setNumber(orderNumber);
    order.setStatus(Boolean.FALSE);
    systemOrderRepository.save(order);

    List<OrderItem> orderItems = items.stream()
        .map(it -> {
          OrderItem oi = new OrderItem();
          oi.setId(UUID.randomUUID());
          oi.setOrderId(orderId);
          oi.setNomenclatureId(it.getNomenclatureId());
          oi.setCount(Optional.ofNullable(it.getCount()).orElse(0));
          oi.setPrice(Optional.ofNullable(it.getPrice()).orElse(0d));
          oi.setSum(Optional.ofNullable(it.getSum()).orElse(0d));
          return oi;
        })
        .toList();
    orderItemRepository.saveAll(orderItems);

    return OrderUploadResponseDto.builder()
        .orderId(orderId)
        .items(orderItems.size())
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
