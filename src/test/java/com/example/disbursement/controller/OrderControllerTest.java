package com.example.disbursement.controller;

import com.example.disbursement.dto.OrderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.exception.ResourceNotFoundException;
import com.example.disbursement.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private UUID orderId;
    private UUID providerId;
    private JsonApiData<OrderDto> testOrderData;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        OrderDto orderDto = OrderDto.builder()
                .providerId(providerId.toString())
                .providerName("Globe")
                .accountNumber("+639123456789")
                .baseAmount(new BigDecimal("100.00"))
                .feeAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("110.00"))
                .status("NEW")
                .build();

        testOrderData = JsonApiData.<OrderDto>builder()
                .type("orders")
                .id(orderId.toString())
                .attributes(orderDto)
                .build();
    }

    @Test
    void createLoadOrder_shouldReturn201() throws Exception {
        when(orderService.createLoadOrder(any(OrderDto.class))).thenReturn(testOrderData);

        String requestBody = String.format("""
            {
                "data": {
                    "type": "orders",
                    "attributes": {
                        "providerId": "%s",
                        "accountNumber": "+639123456789",
                        "amount": 100.00
                    }
                }
            }
            """, providerId);

        mockMvc.perform(post("/orders/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("orders"))
                .andExpect(jsonPath("$.data.attributes.status").value("NEW"))
                .andExpect(jsonPath("$.data.attributes.totalAmount").value(110.00));
    }

    @Test
    void disburseOrder_shouldReturn200() throws Exception {
        UUID paymentId = UUID.randomUUID();
        OrderDto successOrderDto = OrderDto.builder()
                .providerId(providerId.toString())
                .providerName("Globe")
                .accountNumber("+639123456789")
                .baseAmount(new BigDecimal("100.00"))
                .feeAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("110.00"))
                .paymentId(paymentId.toString())
                .status("SUCCESS")
                .build();

        JsonApiData<OrderDto> successOrderData = JsonApiData.<OrderDto>builder()
                .type("orders")
                .id(orderId.toString())
                .attributes(successOrderDto)
                .build();

        when(orderService.disburseOrder(eq(orderId), any(OrderDto.class))).thenReturn(successOrderData);

        String requestBody = String.format("""
            {
                "data": {
                    "type": "orders",
                    "attributes": {
                        "paymentId": "%s"
                    }
                }
            }
            """, paymentId);

        mockMvc.perform(post("/orders/load/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.attributes.paymentId").value(paymentId.toString()));
    }

    @Test
    void getOrder_shouldReturn200() throws Exception {
        when(orderService.getOrderById(orderId)).thenReturn(testOrderData);

        mockMvc.perform(get("/orders/" + orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("orders"))
                .andExpect(jsonPath("$.data.id").value(orderId.toString()))
                .andExpect(jsonPath("$.data.attributes.providerName").value("Globe"));
    }

    @Test
    void getOrder_shouldReturn404WhenNotFound() throws Exception {
        when(orderService.getOrderById(orderId))
                .thenThrow(new ResourceNotFoundException("Order not found with ID: " + orderId));

        mockMvc.perform(get("/orders/" + orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].status").value("404"));
    }
}
