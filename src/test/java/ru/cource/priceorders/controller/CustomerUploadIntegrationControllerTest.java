package ru.cource.priceorders.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.cource.priceorders.models.dto.CustomerUploadResponseDto;
import ru.cource.priceorders.service.CustomerUploadService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerUploadIntegrationController.class)
class CustomerUploadIntegrationControllerTest {

  @Autowired
  private MockMvc mockMvc;


  @MockBean
  private CustomerUploadService customerUploadService;

  @Test
  void uploadAcceptsExpectedCurlPayloadAndReturnsResponse() throws Exception {
    UUID supplierId = UUID.fromString("3f95cf43-e820-2599-3da5-f40fb70da0ab");

    CustomerUploadResponseDto response = CustomerUploadResponseDto.builder()
        .total(1)
        .matched(1)
        .created(1)
        .skipped(0)
        .notFound(0)
        .processed(List.of())
        .notMatched(List.of())
        .build();

    when(customerUploadService.upload(eq(supplierId), anyList())).thenReturn(response);

    mockMvc.perform(post("/api/v1/customer-external-ids")
            .header("secretWord", supplierId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                [
                  {
                    "id": "11111111-2222-3333-4444-555555555555",
                    "inn": "7700000001",
                    "kpp": "770001001"
                  }
                ]
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1))
        .andExpect(jsonPath("$.created").value(1));
  }
}
