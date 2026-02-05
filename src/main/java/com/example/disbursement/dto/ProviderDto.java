package com.example.disbursement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProviderDto {

    @NotBlank(message = "Provider name is required")
    private String name;

    @NotNull(message = "Fee amount is required")
    @Positive(message = "Fee amount must be positive")
    private BigDecimal feeAmount;

    @NotBlank(message = "Validate API URL is required")
    private String validateApiUrl;

    @NotBlank(message = "Disbursement API URL is required")
    private String disbursementApiUrl;

    private String createdAt;
}
