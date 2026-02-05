package com.example.disbursement.controller;

import com.example.disbursement.dto.OrderDto;
import com.example.disbursement.dto.jsonapi.JsonApiData;
import com.example.disbursement.dto.jsonapi.JsonApiRequest;
import com.example.disbursement.dto.jsonapi.JsonApiResponse;
import com.example.disbursement.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";

    private final OrderService orderService;

    @PostMapping(
            path = "/load",
            consumes = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE},
            produces = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<JsonApiResponse<OrderDto>> createLoadOrder(
            @Valid @RequestBody JsonApiRequest<OrderDto> request) {
        log.debug("POST /orders/load - Creating load order");

        JsonApiData<OrderDto> data = orderService.createLoadOrder(request.getData().getAttributes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonApiResponse.single(data));
    }

    @PostMapping(
            path = "/load/{orderId}",
            consumes = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE},
            produces = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<JsonApiResponse<OrderDto>> disburseOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody JsonApiRequest<OrderDto> request) {
        log.debug("POST /orders/load/{} - Disbursing order", orderId);

        JsonApiData<OrderDto> data = orderService.disburseOrder(orderId, request.getData().getAttributes());
        return ResponseEntity.ok(JsonApiResponse.single(data));
    }

    @GetMapping(
            path = "/{orderId}",
            produces = {JSON_API_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<JsonApiResponse<OrderDto>> getOrder(@PathVariable UUID orderId) {
        log.debug("GET /orders/{} - Fetching order", orderId);

        JsonApiData<OrderDto> data = orderService.getOrderById(orderId);
        return ResponseEntity.ok(JsonApiResponse.single(data));
    }
}
