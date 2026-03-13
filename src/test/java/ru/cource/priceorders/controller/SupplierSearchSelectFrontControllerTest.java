package ru.cource.priceorders.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.cource.priceorders.models.dto.SupplierSearchSelectResponseDto;
import ru.cource.priceorders.service.SupplierSearchSelectService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierSearchSelectFrontController.class)
class SupplierSearchSelectFrontControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SupplierSearchSelectService supplierSearchSelectService;

  @Test
  void searchSelectSupportsApiV1Contract() throws Exception {
    UUID supplierId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    when(supplierSearchSelectService.searchSelect(isNull(), eq("бр"))).thenReturn(List.of(
        SupplierSearchSelectResponseDto.builder()
            .id(supplierId)
            .name("Бриз")
            .build()
    ));

    mockMvc.perform(get("/api/v1/suppliers/search-select")
            .param("searchString", "бр"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(supplierId.toString()))
        .andExpect(jsonPath("$[0].name").value("Бриз"));
  }
}
