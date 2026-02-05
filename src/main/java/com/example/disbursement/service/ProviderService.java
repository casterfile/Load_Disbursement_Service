package com.example.disbursement.service;

import com.example.disbursement.dto.ProviderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.entity.Provider;
import com.example.disbursement.exception.ResourceNotFoundException;
import com.example.disbursement.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;

    @Transactional
    public JsonApiData<ProviderDto> createProvider(ProviderDto dto) {
        log.debug("Creating provider: {}", dto.getName());

        Provider provider = Provider.builder()
                .name(dto.getName())
                .feeAmount(dto.getFeeAmount())
                .validateApiUrl(dto.getValidateApiUrl())
                .disbursementApiUrl(dto.getDisbursementApiUrl())
                .build();

        provider = providerRepository.save(provider);
        log.info("Created provider with ID: {}", provider.getId());

        return toJsonApiData(provider);
    }

    @Transactional(readOnly = true)
    public List<JsonApiData<ProviderDto>> getAllProviders() {
        log.debug("Fetching all providers");
        return providerRepository.findAll().stream()
                .map(this::toJsonApiData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Provider getProviderById(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + id));
    }

    private JsonApiData<ProviderDto> toJsonApiData(Provider provider) {
        ProviderDto dto = ProviderDto.builder()
                .name(provider.getName())
                .feeAmount(provider.getFeeAmount())
                .validateApiUrl(provider.getValidateApiUrl())
                .disbursementApiUrl(provider.getDisbursementApiUrl())
                .createdAt(provider.getCreatedAt() != null ? provider.getCreatedAt().toString() : null)
                .build();

        return JsonApiData.<ProviderDto>builder()
                .type("providers")
                .id(provider.getId().toString())
                .attributes(dto)
                .build();
    }
}
