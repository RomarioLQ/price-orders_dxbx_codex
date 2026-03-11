package ru.cource.priceorders.exception.common;

import ru.dxbx.common.dto.error.ParamDto;

import java.util.List;

/**
 * Сущность не найдена (HTTP 404).
 */
public class NotFoundException extends BaseException {

  public NotFoundException(String code, String traceId, String message, String source, List<ParamDto> params) {
    super(code, traceId, message, source, params);
  }
}
