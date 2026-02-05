package com.example.disbursement.service;

import com.example.disbursement.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerApiService {

    private final WebClient webClient;

    public void validateLoad(String validateApiUrl, String accountNumber, BigDecimal amount) {
        log.debug("Calling validate API: {} for account: {} with amount: {}",
                validateApiUrl, accountNumber, amount);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(validateApiUrl)
                    .bodyValue(Map.of(
                            "accountNumber", accountNumber,
                            "amount", amount
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.get("valid"))) {
                String error = response != null ? (String) response.get("error") : "Validation failed";
                throw new ValidationException(error != null ? error : "Validation failed");
            }

            log.debug("Validation successful for account: {}", accountNumber);
        } catch (WebClientResponseException e) {
            log.error("Validation API error: {}", e.getMessage());
            throw new ValidationException("Partner validation service error: " + e.getMessage());
        }
    }

    public boolean disburseLoad(String disbursementApiUrl, UUID orderId, UUID paymentId,
                                String accountNumber, BigDecimal amount) {
        log.debug("Calling disbursement API: {} for order: {}", disbursementApiUrl, orderId);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(disbursementApiUrl)
                    .bodyValue(Map.of(
                            "orderId", orderId.toString(),
                            "paymentId", paymentId.toString(),
                            "accountNumber", accountNumber,
                            "amount", amount
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            boolean success = response != null && Boolean.TRUE.equals(response.get("success"));
            log.debug("Disbursement result for order {}: {}", orderId, success ? "SUCCESS" : "FAILED");
            return success;
        } catch (WebClientResponseException e) {
            log.error("Disbursement API error: {}", e.getMessage());
            return false;
        }
    }
}
