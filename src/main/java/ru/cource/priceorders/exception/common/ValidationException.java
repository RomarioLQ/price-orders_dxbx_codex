package ru.cource.priceorders.exception.common;

import ru.dxbx.common.dto.error.ParamDto;

import java.util.List;

/**
 * Исключение валидации, маппится в HTTP 400 через AppExceptionHandler.
 */
public class ValidationException extends BaseException {

  public ValidationException(String code, String traceId, String message, String source, List<ParamDto> params) {
    super(code, traceId, message, source, params);
  }

  public ValidationException(String code, String traceId, String message) {
    super(code, traceId, message);
  }
}
