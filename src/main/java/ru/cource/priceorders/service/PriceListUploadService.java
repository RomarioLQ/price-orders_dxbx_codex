package ru.cource.priceorders.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cource.priceorders.dao.CustomerExternalIdRepository;
import ru.cource.priceorders.dao.NomenclatureRepository;
import ru.cource.priceorders.dao.PriceListRepository;
import ru.cource.priceorders.dao.PricePositionRepository;
import ru.cource.priceorders.dao.SupplierRepository;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.cource.priceorders.models.CustomerExternalId;
import ru.cource.priceorders.models.Nomenclature;
import ru.cource.priceorders.models.PriceList;
import ru.cource.priceorders.models.PricePosition;
import ru.cource.priceorders.models.dto.PriceListUploadRequestDto;
import ru.cource.priceorders.models.dto.PriceListUploadResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Загрузка прайс-листа в БД (price_list + price_positions + nomenclature).
 */
@Service
@RequiredArgsConstructor
public class PriceListUploadService {

  private final SupplierRepository supplierRepository;
  private final CustomerExternalIdRepository customerExternalIdRepository;
  private final NomenclatureRepository nomenclatureRepository;
  private final PriceListRepository priceListRepository;
  private final PricePositionRepository pricePositionRepository;
  private final TraceIdGenerator traceIdGenerator;

  @Transactional
  public PriceListUploadResponseDto upload(PriceListUploadRequestDto request) {
    UUID supplierId = Optional.ofNullable(request)
        .map(PriceListUploadRequestDto::getSupplier)
        .map(PriceListUploadRequestDto.SupplierRefDto::getId)
        .orElseThrow(() -> validation("supplier.id", "Supplier id is required"));

    if (!supplierRepository.existsById(supplierId)) {
      throw new NotFoundException(
          "SUPPLIER_NOT_FOUND",
          traceIdGenerator.gen(),
          "Supplier is not found",
          null,
          List.of(ParamDto.builder().key("supplierId").value(supplierId.toString()).build())
      );
    }

    UUID customerExternalId = Optional.ofNullable(request.getCustomerExternalId())
        .orElseThrow(() -> validation("customer_id", "customer_id is required"));

    UUID customerId = resolveCustomerId(supplierId, customerExternalId);

    PriceListUploadRequestDto.PriceListDto plDto = Optional.ofNullable(request.getPriceList())
        .orElseThrow(() -> validation("price_list", "price_list is required"));

    LocalDateTime startDate = Optional.ofNullable(plDto.getStartDate())
        .orElseThrow(() -> validation("price_list.start_date", "start_date is required"));

    priceListRepository.findFirstBySupplierIdAndCustomerIdOrderByStartDateDesc(supplierId, customerId)
        .ifPresent(existing -> {
          pricePositionRepository.deleteAllByPriceId(existing.getId());
          priceListRepository.deleteById(existing.getId());
        });

    UUID priceId = UUID.randomUUID();

    PriceList priceList = new PriceList();
    priceList.setId(priceId);
    priceList.setSupplierId(supplierId);
    priceList.setCustomerId(customerId);
    priceList.setStartDate(startDate);
    priceList.setEndDate(plDto.getEndDate());
    priceList.setActive(true);
    priceListRepository.save(priceList);

    List<PriceListUploadRequestDto.PriceListPositionDto> positions = Optional.ofNullable(request.getPositions())
        .orElseThrow(() -> validation("positions", "positions is required"));

    if (positions.isEmpty()) {
      throw validation("positions", "positions must not be empty");
    }

    Set<UUID> uniqueNomenclatureIds = new HashSet<>();
    AtomicInteger createdNomenclature = new AtomicInteger(0);

    List<PricePosition> pricePositionsToSave = positions.stream()
        .map(pos -> {
          if (pos == null) {
            throw validation("positions", "position item is null");
          }

          String code = Optional.ofNullable(pos.getCode()).orElse("").trim();
          String name = Optional.ofNullable(pos.getName()).orElse("").trim();

          if (code.isBlank()) {
            throw validation("positions.code", "code is required");
          }
          if (name.isBlank()) {
            throw validation("positions.name", "name is required");
          }
          if (pos.getPriceWithVat() == null) {
            throw validation("positions.price_with_vat", "price_with_vat is required");
          }
          if (pos.getVat() == null) {
            throw validation("positions.vat", "vat is required");
          }

          UUID nomenclatureId = Optional.ofNullable(pos.getNomenclatureId()).orElse(UUID.randomUUID());

          Nomenclature nomenclature = nomenclatureRepository.findById(nomenclatureId)
              .map(existing -> updateNomenclature(existing, supplierId, code, name, nomenclatureId))
              .orElseGet(() -> createNomenclature(supplierId, nomenclatureId, code, name, createdNomenclature));

          if (!uniqueNomenclatureIds.add(nomenclature.getId())) {
            throw validation(
                "positions.nomenclature_id",
                "duplicate nomenclature in request",
                List.of(ParamDto.builder().key("nomenclatureId").value(nomenclature.getId().toString()).build())
            );
          }

          PricePosition pp = new PricePosition();
          pp.setId(UUID.randomUUID());
          pp.setPriceId(priceId);
          pp.setNomenclatureId(nomenclature.getId());
          pp.setUnitMeasure(pos.getUnitMeasure());
          pp.setPriceWithVat(pos.getPriceWithVat());
          pp.setVat(pos.getVat());
          return pp;
        })
        .toList();

    pricePositionRepository.saveAll(pricePositionsToSave);

    return PriceListUploadResponseDto.builder()
        .priceId(priceId)
        .createdNomenclature(createdNomenclature.get())
        .positions(pricePositionsToSave.size())
        .build();
  }

  private UUID resolveCustomerId(UUID supplierId, UUID customerExternalId) {
    return customerExternalIdRepository.findFirstByCustomerExternalIdAndSupplierId(customerExternalId, supplierId)
        .map(CustomerExternalId::getCustomerId)
        .orElseThrow(() -> new NotFoundException(
            "CUSTOMER_EXTERNAL_ID_NOT_FOUND",
            traceIdGenerator.gen(),
            "Customer external id is not found for supplier",
            null,
            List.of(
                ParamDto.builder().key("supplierId").value(supplierId.toString()).build(),
                ParamDto.builder().key("customerExternalId").value(customerExternalId.toString()).build()
            )
        ));
  }

  private Nomenclature updateNomenclature(
      Nomenclature existing,
      UUID supplierId,
      String code,
      String name,
      UUID nomenclatureId
  ) {
    if (!supplierId.equals(existing.getSupplierId())) {
      throw validation(
          "positions.nomenclature_id",
          "nomenclature supplier mismatch",
          List.of(
              ParamDto.builder().key("supplierId").value(supplierId.toString()).build(),
              ParamDto.builder().key("nomenclatureId").value(nomenclatureId.toString()).build()
          )
      );
    }

    existing.setCode(code);
    existing.setName(name);
    return nomenclatureRepository.save(existing);
  }

  private Nomenclature createNomenclature(
      UUID supplierId,
      UUID nomenclatureId,
      String code,
      String name,
      AtomicInteger createdNomenclature
  ) {
    Nomenclature nomenclature = new Nomenclature();
    nomenclature.setId(nomenclatureId);
    nomenclature.setSupplierId(supplierId);
    nomenclature.setCode(code);
    nomenclature.setName(name);
    createdNomenclature.incrementAndGet();
    return nomenclatureRepository.save(nomenclature);
  }

  private ValidationException validation(String key, String message) {
    return validation(key, message, List.of(ParamDto.builder().key(key).value(message).build()));
  }

  private ValidationException validation(String key, String message, List<ParamDto> params) {
    return new ValidationException(
        "PARAMS_VALIDATION_FAILED",
        traceIdGenerator.gen(),
        message,
        null,
        params
    );
  }
}
