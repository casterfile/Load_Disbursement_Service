package com.example.disbursement.controller;

import com.example.disbursement.dto.ProviderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.dto.jsonapi.JsonApiRequest;
import com.example.disbursement.dto.jsonapi.JsonApiResponse;
import com.example.disbursement.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/providers")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {

    private static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";

    private final ProviderService providerService;

    @PostMapping(
            consumes = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE},
            produces = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<JsonApiResponse<ProviderDto>> createProvider(
            @Valid @RequestBody JsonApiRequest<ProviderDto> request) {
        log.debug("POST /providers - Creating provider");

        JsonApiData<ProviderDto> data = providerService.createProvider(request.getData().getAttributes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonApiResponse.single(data));
    }

    @GetMapping(produces = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<JsonApiResponse<ProviderDto>> getAllProviders() {
        log.debug("GET /providers - Fetching all providers");

        List<JsonApiData<ProviderDto>> providers = providerService.getAllProviders();
        return ResponseEntity.ok(JsonApiResponse.list(providers));
    }
}
