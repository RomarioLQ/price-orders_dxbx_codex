package ru.cource.priceorders.exception.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.IntStream;

/**
 * Генератор traceId (по смыслу как в product-control).
 */
@Component
public class TraceIdGenerator {

  @Value("${integration.trace.generator.prefix:price-orders-}")
  private String prefix;

  @Value("${integration.trace.generator.maxLength:6}")
  private int maxLength;

  private final SecureRandom random = new SecureRandom();

  public String gen() {
    StringBuilder sb = new StringBuilder(prefix);
    IntStream.range(0, maxLength).forEach(i -> sb.append(random.nextInt(10)));
    return sb.toString();
  }
}
