package ru.cource.priceorders.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.cource.priceorders.models.dto.OrderMarkUploadedResponseDto;
import ru.cource.priceorders.models.dto.SupplierOrderUnprocessedResponseDto;
import ru.cource.priceorders.service.SupplierOrderIntegrationService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierOrderIntegrationController.class)
class SupplierOrderIntegrationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SupplierOrderIntegrationService supplierOrderIntegrationService;

  @Test
  void getUnprocessedAcceptsSupplierIdContract() throws Exception {
    UUID supplierId = UUID.fromString("a2b820b5-7ed1-49ee-8c8d-0a0f9c6b8d36");
    UUID orderId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    when(supplierOrderIntegrationService.getUnprocessed(supplierId)).thenReturn(List.of(
        SupplierOrderUnprocessedResponseDto.builder()
            .id(orderId)
            .supplierId(supplierId)
            .items(List.of())
            .build()
    ));

    mockMvc.perform(get("/api/v1/orders/unprocessed")
            .param("supplierId", supplierId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(orderId.toString()))
        .andExpect(jsonPath("$[0].supplier_id").value(supplierId.toString()));
  }

  @Test
  void markUploadedReturnsServiceResponse() throws Exception {
    UUID orderId = UUID.randomUUID();
    when(supplierOrderIntegrationService.markUploaded(any())).thenReturn(OrderMarkUploadedResponseDto.builder()
        .orderId(orderId)
        .status(Boolean.TRUE)
        .build());

    mockMvc.perform(post("/api/v1/orders/mark-uploaded")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "order_id": "%s"
                }
                """.formatted(orderId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.order_id").value(orderId.toString()))
        .andExpect(jsonPath("$.status").value(true));
  }
}
