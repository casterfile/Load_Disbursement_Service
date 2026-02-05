package com.example.disbursement.service;

import com.example.disbursement.dto.OrderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.entity.Order;
import com.example.disbursement.entity.Provider;
import com.example.disbursement.exception.ResourceNotFoundException;
import com.example.disbursement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProviderService providerService;

    @Mock
    private PartnerApiService partnerApiService;

    @InjectMocks
    private OrderService orderService;

    private Provider testProvider;
    private Order testOrder;
    private UUID providerId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        testProvider = Provider.builder()
                .id(providerId)
                .name("Globe")
                .feeAmount(new BigDecimal("10.00"))
                .validateApiUrl("http://wiremock:8080/validate")
                .disbursementApiUrl("http://wiremock:8080/disburse")
                .createdAt(LocalDateTime.now())
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .providerId(providerId)
                .providerName("Globe")
                .accountNumber("+639123456789")
                .baseAmount(new BigDecimal("100.00"))
                .feeAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("110.00"))
                .status(Order.OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createLoadOrder_shouldCreateOrder() {
        OrderDto requestDto = OrderDto.builder()
                .providerId(providerId.toString())
                .accountNumber("+639123456789")
                .amount(new BigDecimal("100.00"))
                .build();

        when(providerService.getProviderById(providerId)).thenReturn(testProvider);
        doNothing().when(partnerApiService).validateLoad(any(), any(), any());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        JsonApiData<OrderDto> result = orderService.createLoadOrder(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("orders");
        assertThat(result.getAttributes().getStatus()).isEqualTo("NEW");
        assertThat(result.getAttributes().getTotalAmount()).isEqualByComparingTo(new BigDecimal("110.00"));

        verify(partnerApiService).validateLoad(
                eq(testProvider.getValidateApiUrl()),
                eq("+639123456789"),
                eq(new BigDecimal("100.00"))
        );
    }

    @Test
    void disburseOrder_shouldUpdateOrderToSuccess() {
        UUID paymentId = UUID.randomUUID();
        OrderDto requestDto = OrderDto.builder()
                .paymentId(paymentId.toString())
                .build();

        Order successOrder = Order.builder()
                .id(orderId)
                .providerId(providerId)
                .providerName("Globe")
                .accountNumber("+639123456789")
                .baseAmount(new BigDecimal("100.00"))
                .feeAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("110.00"))
                .status(Order.OrderStatus.SUCCESS)
                .paymentId(paymentId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(providerService.getProviderById(providerId)).thenReturn(testProvider);
        when(partnerApiService.disburseLoad(any(), any(), any(), any(), any())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(successOrder);

        JsonApiData<OrderDto> result = orderService.disburseOrder(orderId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getAttributes().getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getAttributes().getPaymentId()).isEqualTo(paymentId.toString());
    }

    @Test
    void disburseOrder_shouldUpdateOrderToFailed() {
        UUID paymentId = UUID.randomUUID();
        OrderDto requestDto = OrderDto.builder()
                .paymentId(paymentId.toString())
                .build();

        Order failedOrder = Order.builder()
                .id(orderId)
                .providerId(providerId)
                .providerName("Globe")
                .accountNumber("+639123456789")
                .baseAmount(new BigDecimal("100.00"))
                .feeAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("110.00"))
                .status(Order.OrderStatus.FAILED)
                .paymentId(paymentId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(providerService.getProviderById(providerId)).thenReturn(testProvider);
        when(partnerApiService.disburseLoad(any(), any(), any(), any(), any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(failedOrder);

        JsonApiData<OrderDto> result = orderService.disburseOrder(orderId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getAttributes().getStatus()).isEqualTo("FAILED");
    }

    @Test
    void disburseOrder_shouldThrowWhenOrderAlreadyDisbursed() {
        testOrder.setStatus(Order.OrderStatus.SUCCESS);
        OrderDto requestDto = OrderDto.builder()
                .paymentId(UUID.randomUUID().toString())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.disburseOrder(orderId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order cannot be disbursed");
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        JsonApiData<OrderDto> result = orderService.getOrderById(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId.toString());
        assertThat(result.getAttributes().getProviderName()).isEqualTo("Globe");
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }
}
