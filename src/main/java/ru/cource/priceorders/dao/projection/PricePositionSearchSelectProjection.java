package ru.cource.priceorders.dao.projection;

import java.util.UUID;

/**
 * Проекция для нативного select (search-select) по позициям прайс-листа.
 */
public interface PricePositionSearchSelectProjection {

  UUID getPriceId();

  UUID getNomenclatureId();

  String getName();

  String getUnitMeasure();

  Double getPriceWithVat();
}
