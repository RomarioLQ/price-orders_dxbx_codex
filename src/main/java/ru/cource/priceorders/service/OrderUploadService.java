package ru.cource.priceorders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.AddressRepository;
import ru.cource.priceorders.dao.NomenclatureRepository;
import ru.cource.priceorders.dao.OrderItemRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.dao.SystemOrderRepository;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.filter.TelegramUserData;
import ru.cource.priceorders.models.Nomenclature;
import ru.cource.priceorders.models.OrderItem;
import ru.cource.priceorders.models.SystemOrder;
import ru.cource.priceorders.models.dto.OrderUploadRequestDto;
import ru.cource.priceorders.models.dto.OrderUploadResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderUploadService {

  private final SupplierRepository supplierRepository;
  private final TelegramUserContextResolverService telegramUserContextResolverService;
  private final SystemOrderRepository systemOrderRepository;
  private final OrderItemRepository orderItemRepository;
  private final NomenclatureRepository nomenclatureRepository;
  private final AddressRepository addressRepository;
  private final TraceIdGenerator traceIdGenerator;

  @Transactional
  public OrderUploadResponseDto upload(OrderUploadRequestDto request, TelegramUserData telegramUserData) {
    if (request == null) {
      throw validation("request", "request body is required");
    }

    UUID supplierId = Optional.ofNullable(request.getSupplierId())
        .orElseThrow(() -> validation("supplier_id", "supplier_id is required"));
    UUID addressId = Optional.ofNullable(request.getAddressId())
        .orElseThrow(() -> validation("address_id", "address_id is required"));

    var orderDatetime = Optional.ofNullable(request.getDatetime())
        .orElseThrow(() -> validation("datetime", "datetime is required"));

    TelegramUserContextResolverService.TelegramUserContext telegramUserContext =
        telegramUserContextResolverService.resolve(telegramUserData);
    UUID userId = telegramUserContext.userId();
    UUID customerId = telegramUserContext.customerId();

    List<OrderUploadRequestDto.OrderItemDto> items = Optional.ofNullable(request.getItems()).orElse(List.of());
    if (items.isEmpty()) {
      throw validation("items", "items must not be empty");
    }

    validateSupplierExists(supplierId);
    validateAddressBelongsToCustomer(addressId, customerId);
    validateItems(items, supplierId);

    UUID orderId = UUID.randomUUID();
    SystemOrder order = new SystemOrder();
    order.setId(orderId);
    order.setCustomerId(customerId);
    order.setSupplierId(supplierId);
    order.setAddressId(addressId);
    order.setUserId(userId);
    order.setDatetime(orderDatetime);
    order.setNumber(orderId.toString());
    order.setStatus(Boolean.FALSE);
    systemOrderRepository.save(order);

    List<OrderItem> orderItems = items.stream()
        .map(toOrderItem(orderId))
        .toList();
    orderItemRepository.saveAll(orderItems);

    return OrderUploadResponseDto.builder()
        .orderId(orderId)
        .items(orderItems.size())
        .build();
  }

  private void validateSupplierExists(UUID supplierId) {
    if (!supplierRepository.existsById(supplierId)) {
      throw new NotFoundException(
          "SUPPLIER_NOT_FOUND",
          traceIdGenerator.gen(),
          "Supplier is not found",
          null,
          List.of(ParamDto.builder().key("supplierId").value(supplierId.toString()).build())
      );
    }
  }

  private void validateAddressBelongsToCustomer(UUID addressId, UUID customerId) {
    addressRepository.findById(addressId)
        .filter(address -> customerId.equals(address.getCustomerId()))
        .orElseThrow(() -> new ValidationException(
            "ADDRESS_NOT_ALLOWED",
            traceIdGenerator.gen(),
            "Address is not available for current customer",
            null,
            List.of(
                ParamDto.builder().key("addressId").value(addressId.toString()).build(),
                ParamDto.builder().key("customerId").value(customerId.toString()).build()
            )
        ));
  }

  private void validateItems(List<OrderUploadRequestDto.OrderItemDto> items, UUID supplierId) {
    IntStream.range(0, items.size()).forEach(i -> {
      OrderUploadRequestDto.OrderItemDto it = items.get(i);
      UUID nomenclatureId = it == null ? null : it.getNomenclatureId();
      if (nomenclatureId == null) {
        throw validation("items[" + i + "].nomenclature_id", "nomenclature_id is required");
      }

      Nomenclature nomenclature = nomenclatureRepository.findById(nomenclatureId)
          .orElseThrow(() -> new NotFoundException(
              "NOMENCLATURE_NOT_FOUND",
              traceIdGenerator.gen(),
              "Nomenclature is not found",
              null,
              List.of(
                  ParamDto.builder().key("nomenclatureId").value(nomenclatureId.toString()).build(),
                  ParamDto.builder().key("itemIndex").value(String.valueOf(i)).build()
              )
          ));

      if (!supplierId.equals(nomenclature.getSupplierId())) {
        throw new ValidationException(
            "NOMENCLATURE_SUPPLIER_MISMATCH",
            traceIdGenerator.gen(),
            "Nomenclature does not belong to supplier",
            null,
            List.of(
                ParamDto.builder().key("supplierId").value(supplierId.toString()).build(),
                ParamDto.builder().key("nomenclatureId").value(nomenclatureId.toString()).build(),
                ParamDto.builder().key("itemIndex").value(String.valueOf(i)).build()
            )
        );
      }
    });
  }

  private Function<OrderUploadRequestDto.OrderItemDto, OrderItem> toOrderItem(UUID orderId) {
    return it -> {
      OrderItem oi = new OrderItem();
      oi.setId(UUID.randomUUID());
      oi.setOrderId(orderId);
      oi.setNomenclatureId(it.getNomenclatureId());
      oi.setCount(Optional.ofNullable(it.getCount()).orElse(0));
      oi.setPrice(Optional.ofNullable(it.getPrice()).orElse(0d));
      oi.setSum(Optional.ofNullable(it.getSum()).orElse(0d));
      return oi;
    };
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
