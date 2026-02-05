package com.example.disbursement.service;

import com.example.disbursement.dto.OrderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.entity.Order;
import com.example.disbursement.entity.Provider;
import com.example.disbursement.exception.ResourceNotFoundException;
import com.example.disbursement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProviderService providerService;
    private final PartnerApiService partnerApiService;

    @Transactional
    public JsonApiData<OrderDto> createLoadOrder(OrderDto dto) {
        log.debug("Creating load order for provider: {} account: {}",
                dto.getProviderId(), dto.getAccountNumber());

        UUID providerId = UUID.fromString(dto.getProviderId());
        Provider provider = providerService.getProviderById(providerId);

        // Validate with partner API
        partnerApiService.validateLoad(
                provider.getValidateApiUrl(),
                dto.getAccountNumber(),
                dto.getAmount()
        );

        // Calculate amounts
        BigDecimal baseAmount = dto.getAmount();
        BigDecimal feeAmount = provider.getFeeAmount();
        BigDecimal totalAmount = baseAmount.add(feeAmount);

        // Create order
        Order order = Order.builder()
                .providerId(providerId)
                .providerName(provider.getName())
                .accountNumber(dto.getAccountNumber())
                .baseAmount(baseAmount)
                .feeAmount(feeAmount)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.NEW)
                .build();

        order = orderRepository.save(order);
        log.info("Created order with ID: {}", order.getId());

        return toJsonApiData(order);
    }

    @Transactional
    public JsonApiData<OrderDto> disburseOrder(UUID orderId, OrderDto dto) {
        log.debug("Disbursing order: {} with payment: {}", orderId, dto.getPaymentId());

        Order order = getOrderEntityById(orderId);

        // Validate order state
        if (order.getStatus() != Order.OrderStatus.NEW) {
            throw new IllegalStateException(
                    "Order cannot be disbursed. Current status: " + order.getStatus());
        }

        UUID paymentId = UUID.fromString(dto.getPaymentId());
        Provider provider = providerService.getProviderById(order.getProviderId());

        // Call partner disbursement API
        boolean success = partnerApiService.disburseLoad(
                provider.getDisbursementApiUrl(),
                orderId,
                paymentId,
                order.getAccountNumber(),
                order.getBaseAmount()
        );

        // Update order
        order.setPaymentId(paymentId);
        order.setStatus(success ? Order.OrderStatus.SUCCESS : Order.OrderStatus.FAILED);
        order = orderRepository.save(order);

        log.info("Order {} disbursement result: {}", orderId, order.getStatus());
        return toJsonApiData(order);
    }

    @Transactional(readOnly = true)
    public JsonApiData<OrderDto> getOrderById(UUID orderId) {
        log.debug("Fetching order: {}", orderId);
        Order order = getOrderEntityById(orderId);
        return toJsonApiData(order);
    }

    private Order getOrderEntityById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    private JsonApiData<OrderDto> toJsonApiData(Order order) {
        OrderDto dto = OrderDto.builder()
                .providerId(order.getProviderId().toString())
                .providerName(order.getProviderName())
                .accountNumber(order.getAccountNumber())
                .baseAmount(order.getBaseAmount())
                .feeAmount(order.getFeeAmount())
                .totalAmount(order.getTotalAmount())
                .paymentId(order.getPaymentId() != null ? order.getPaymentId().toString() : null)
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null)
                .build();

        return JsonApiData.<OrderDto>builder()
                .type("orders")
                .id(order.getId().toString())
                .attributes(dto)
                .build();
    }
}
