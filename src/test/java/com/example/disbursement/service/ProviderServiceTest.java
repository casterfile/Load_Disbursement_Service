package com.example.disbursement.service;

import com.example.disbursement.dto.ProviderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.entity.Provider;
import com.example.disbursement.exception.ResourceNotFoundException;
import com.example.disbursement.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    private Provider testProvider;
    private ProviderDto testProviderDto;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                .id(UUID.randomUUID())
                .name("Globe")
                .feeAmount(new BigDecimal("10.00"))
                .validateApiUrl("http://wiremock:8080/validate")
                .disbursementApiUrl("http://wiremock:8080/disburse")
                .createdAt(LocalDateTime.now())
                .build();

        testProviderDto = ProviderDto.builder()
                .name("Globe")
                .feeAmount(new BigDecimal("10.00"))
                .validateApiUrl("http://wiremock:8080/validate")
                .disbursementApiUrl("http://wiremock:8080/disburse")
                .build();
    }

    @Test
    void createProvider_shouldCreateAndReturnProvider() {
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);

        JsonApiData<ProviderDto> result = providerService.createProvider(testProviderDto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("providers");
        assertThat(result.getId()).isEqualTo(testProvider.getId().toString());
        assertThat(result.getAttributes().getName()).isEqualTo("Globe");
        assertThat(result.getAttributes().getFeeAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void getAllProviders_shouldReturnAllProviders() {
        Provider provider2 = Provider.builder()
                .id(UUID.randomUUID())
                .name("Smart")
                .feeAmount(new BigDecimal("15.00"))
                .validateApiUrl("http://wiremock:8080/validate")
                .disbursementApiUrl("http://wiremock:8080/disburse")
                .createdAt(LocalDateTime.now())
                .build();

        when(providerRepository.findAll()).thenReturn(Arrays.asList(testProvider, provider2));

        List<JsonApiData<ProviderDto>> result = providerService.getAllProviders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAttributes().getName()).isEqualTo("Globe");
        assertThat(result.get(1).getAttributes().getName()).isEqualTo("Smart");
    }

    @Test
    void getAllProviders_shouldReturnEmptyListWhenNoProviders() {
        when(providerRepository.findAll()).thenReturn(List.of());

        List<JsonApiData<ProviderDto>> result = providerService.getAllProviders();

        assertThat(result).isEmpty();
    }

    @Test
    void getProviderById_shouldReturnProvider() {
        UUID providerId = testProvider.getId();
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(testProvider));

        Provider result = providerService.getProviderById(providerId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Globe");
    }

    @Test
    void getProviderById_shouldThrowWhenNotFound() {
        UUID providerId = UUID.randomUUID();
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.getProviderById(providerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Provider not found");
    }
}
