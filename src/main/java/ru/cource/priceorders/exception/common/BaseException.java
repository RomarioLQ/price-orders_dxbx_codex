package ru.cource.priceorders.exception.common;

import lombok.Getter;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.Collections;
import java.util.List;

/**
 * Базовое доменное исключение по образцу ...
 */
@Getter
public abstract class BaseException extends RuntimeException {

  private final String code;
  private final String traceId;
  private final String source;
  private final List<ParamDto> params;

  protected BaseException(String code, String traceId, String message, String source, List<ParamDto> params) {
    super(message);
    this.code = code;
    this.traceId = traceId;
    this.source = source;
    this.params = params == null ? Collections.emptyList() : params;
  }

  protected BaseException(String code, String traceId, String message) {
    this(code, traceId, message, null, Collections.emptyList());
  }
}
