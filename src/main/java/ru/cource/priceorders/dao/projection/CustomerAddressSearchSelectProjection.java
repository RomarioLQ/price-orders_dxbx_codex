package ru.cource.priceorders.dao.projection;

import java.util.UUID;

/**
 * Проекция для search-select адресов клиента.
 */
public interface CustomerAddressSearchSelectProjection {

  UUID getId();

  String getAddress();
}
