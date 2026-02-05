package com.example.disbursement.exception;

import com.example.disbursement.dto.jsonapi.JsonApiError;
import com.example.disbursement.dto.jsonapi.JsonApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<JsonApiResponse<?>> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        JsonApiError error = JsonApiError.builder()
                .status("422")
                .title("Validation Error")
                .detail(ex.getMessage())
                .code("VALIDATION_ERROR")
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(JsonApiResponse.error(List.of(error)));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<JsonApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        JsonApiError error = JsonApiError.builder()
                .status("404")
                .title("Not Found")
                .detail(ex.getMessage())
                .code("RESOURCE_NOT_FOUND")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(JsonApiResponse.error(List.of(error)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        List<JsonApiError> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String field = error instanceof FieldError ?
                            ((FieldError) error).getField() : error.getObjectName();
                    return JsonApiError.builder()
                            .status("422")
                            .title("Validation Error")
                            .detail(field + ": " + error.getDefaultMessage())
                            .code("INVALID_ATTRIBUTE")
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(JsonApiResponse.error(errors));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<JsonApiResponse<?>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        JsonApiError error = JsonApiError.builder()
                .status("409")
                .title("Conflict")
                .detail(ex.getMessage())
                .code("ILLEGAL_STATE")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(JsonApiResponse.error(List.of(error)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        JsonApiError error = JsonApiError.builder()
                .status("500")
                .title("Internal Server Error")
                .detail("An unexpected error occurred")
                .code("INTERNAL_ERROR")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JsonApiResponse.error(List.of(error)));
    }
}
