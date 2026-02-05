package com.example.disbursement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {

    // For create order request
    private String providerId;

    @Pattern(regexp = "^\\+63\\d{10}$", message = "Account number must be a valid Philippine mobile number (e.g., +639123456789)")
    private String accountNumber;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    // For disburse request
    private String paymentId;

    // For response
    private String providerName;
    private BigDecimal baseAmount;
    private BigDecimal feeAmount;
    private BigDecimal totalAmount;
    private String status;
    private String createdAt;
    private String updatedAt;
}
