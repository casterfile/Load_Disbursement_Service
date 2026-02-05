package com.example.disbursement.controller;

import com.example.disbursement.dto.ProviderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.service.ProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProviderService providerService;

    private JsonApiData<ProviderDto> testProviderData;

    @BeforeEach
    void setUp() {
        ProviderDto providerDto = ProviderDto.builder()
                .name("Globe")
                .feeAmount(new BigDecimal("10.00"))
                .validateApiUrl("http://wiremock:8080/validate")
                .disbursementApiUrl("http://wiremock:8080/disburse")
                .build();

        testProviderData = JsonApiData.<ProviderDto>builder()
                .type("providers")
                .id(UUID.randomUUID().toString())
                .attributes(providerDto)
                .build();
    }

    @Test
    void createProvider_shouldReturn201() throws Exception {
        when(providerService.createProvider(any(ProviderDto.class))).thenReturn(testProviderData);

        String requestBody = """
            {
                "data": {
                    "type": "providers",
                    "attributes": {
                        "name": "Globe",
                        "feeAmount": 10.00,
                        "validateApiUrl": "http://wiremock:8080/validate",
                        "disbursementApiUrl": "http://wiremock:8080/disburse"
                    }
                }
            }
            """;

        mockMvc.perform(post("/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("providers"))
                .andExpect(jsonPath("$.data.attributes.name").value("Globe"))
                .andExpect(jsonPath("$.data.attributes.feeAmount").value(10.00));
    }

    @Test
    void getAllProviders_shouldReturnProvidersList() throws Exception {
        ProviderDto provider2Dto = ProviderDto.builder()
                .name("Smart")
                .feeAmount(new BigDecimal("15.00"))
                .validateApiUrl("http://wiremock:8080/validate")
                .disbursementApiUrl("http://wiremock:8080/disburse")
                .build();

        JsonApiData<ProviderDto> provider2Data = JsonApiData.<ProviderDto>builder()
                .type("providers")
                .id(UUID.randomUUID().toString())
                .attributes(provider2Dto)
                .build();

        List<JsonApiData<ProviderDto>> providers = Arrays.asList(testProviderData, provider2Data);
        when(providerService.getAllProviders()).thenReturn(providers);

        mockMvc.perform(get("/providers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].attributes.name").value("Globe"))
                .andExpect(jsonPath("$.data[1].attributes.name").value("Smart"));
    }

    @Test
    void getAllProviders_shouldReturnEmptyList() throws Exception {
        when(providerService.getAllProviders()).thenReturn(List.of());

        mockMvc.perform(get("/providers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
