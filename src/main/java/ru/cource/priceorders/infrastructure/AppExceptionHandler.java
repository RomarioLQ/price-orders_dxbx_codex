package ru.cource.priceorders.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.cource.priceorders.exception.common.BaseException;
import ru.cource.priceorders.exception.common.NotFoundException;
import ru.cource.priceorders.exception.common.ValidationException;
import ru.cource.priceorders.exception.generator.TraceIdGenerator;
import ru.dxbx.common.dto.error.ErrorResponseDto;
import ru.dxbx.common.dto.error.ParamDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Централизованная обработка ошибок контроллеров.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class AppExceptionHandler {

  @Value("${integration.source:price-orders}")
  private String source;

  private final TraceIdGenerator traceIdGenerator;

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {
    ErrorResponseDto response = toErrorResponse(ex);
    log.warn("Validation error, traceId: {}, code: {}, source: {}, message: {}, params: {}",
        response.getTraceId(), response.getCode(), response.getSource(), ex.getMessage(), response.getParams(), ex);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNotFoundException(NotFoundException ex) {
    ErrorResponseDto response = toErrorResponse(ex);
    log.warn("Not found, traceId: {}, code: {}, source: {}, message: {}, params: {}",
        response.getTraceId(), response.getCode(), response.getSource(), ex.getMessage(), response.getParams(), ex);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponseDto> handleBaseException(BaseException ex) {
    ErrorResponseDto response = toErrorResponse(ex);
    log.error("Business error, traceId: {}, code: {}, source: {}, message: {}, params: {}",
        response.getTraceId(), response.getCode(), response.getSource(), ex.getMessage(), response.getParams(), ex);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String traceId = traceIdGenerator.gen();
    ErrorResponseDto response = ErrorResponseDto.builder()
        .code("PARAMS_VALIDATION_FAILED")
        .traceId(traceId)
        .source(source)
        .params(convertValidationErrors(ex))
        .build();

    log.warn("Validation error, traceId: {}, code: {}, source: {}, message: {}, params: {}",
        response.getTraceId(), response.getCode(), response.getSource(), ex.getMessage(), response.getParams(), ex);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    String traceId = traceIdGenerator.gen();

    String details = Optional.ofNullable(ex.getMostSpecificCause())
            .map(Throwable::getMessage)
            .orElse(ex.getMessage());

    ErrorResponseDto response = ErrorResponseDto.builder()
            .code("PARAMS_VALIDATION_FAILED")
            .traceId(traceId)
            .source(source)
            .params(List.of(ParamDto.builder().key("message").value(details).build()))
            .build();

    log.warn("Failed to read request, traceId: {}, code: {}, source: {}, details: {}",
            response.getTraceId(), response.getCode(), response.getSource(), details, ex);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }


  @ExceptionHandler(TypeMismatchException.class)
  public ResponseEntity<ErrorResponseDto> handleTypeMismatch(TypeMismatchException ex) {
    String traceId = traceIdGenerator.gen();

    ParamDto errorParam = ParamDto.builder()
        .key(ex.getPropertyName())
        .value(Objects.toString(ex.getValue()))
        .build();

    ErrorResponseDto response = ErrorResponseDto.builder()
        .code("INVALID_PARAMETER_TYPE")
        .traceId(traceId)
        .source(source)
        .params(List.of(errorParam))
        .build();

    log.warn("Type mismatch, traceId: {}, code: {}, source: {}, param: {}",
        response.getTraceId(), response.getCode(), response.getSource(), errorParam, ex);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex) {
    String traceId = traceIdGenerator.gen();
    ErrorResponseDto response = ErrorResponseDto.builder()
        .code("UNEXPECTED_ERROR")
        .traceId(traceId)
        .source(source)
        .params(List.of(ParamDto.builder().key("db").value("Data integrity violation").build()))
        .build();

    log.error("DB integrity violation, traceId: {}, code: {}, source: {}",
        response.getTraceId(), response.getCode(), response.getSource(), ex);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleUnknownException(Exception ex) {
    String traceId = traceIdGenerator.gen();
    log.error("Unexpected error traceId {}", traceId, ex);

    List<ParamDto> params = new ArrayList<>();
    if (ex.getMessage() != null) {
      params.add(ParamDto.builder().key("message").value(ex.getMessage()).build());
    }

    ErrorResponseDto response = ErrorResponseDto.builder()
        .code("UNEXPECTED_ERROR")
        .traceId(traceId)
        .source(source)
        .params(params)
        .build();

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }


  private ErrorResponseDto toErrorResponse(BaseException ex) {
    return ErrorResponseDto.builder()
        .code(ex.getCode())
        .traceId(ex.getTraceId())
        .params(ex.getParams())
        .source(Optional.ofNullable(ex.getSource()).orElse(source))
        .build();
  }

  private List<ParamDto> convertValidationErrors(MethodArgumentNotValidException ex) {
    return ex.getBindingResult().getAllErrors().stream()
        .map(err -> {
          String key = err.getObjectName();
          String value = err instanceof FieldError fe
              ? fe.getField() + " " + fe.getDefaultMessage()
              : err.getDefaultMessage();

          return ParamDto.builder()
              .key(key)
              .value(value)
              .build();
        })
        .toList();
  }
}
