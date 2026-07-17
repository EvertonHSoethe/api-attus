package com.evertonhsoethe.apiattus.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaseNotFound(CaseNotFoundException ex) {
        log.warn("Case not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(DuplicateCaseNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCaseNumber(DuplicateCaseNumberException ex) {
        log.warn("Duplicate case number: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fields.put(field, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed", fields, LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                        "An unexpected error occurred. Please try again later.", LocalDateTime.now()));
    }

    @Schema(description = "Standard error response")
    public record ErrorResponse(
            @Schema(example = "404") int status,
            @Schema(example = "Not Found") String error,
            @Schema(example = "Legal case not found with id: 1") String message,
            @Schema(example = "2024-06-01T10:00:00") LocalDateTime timestamp
    ) {}

    @Schema(description = "Validation error response")
    public record ValidationErrorResponse(
            @Schema(example = "400") int status,
            @Schema(example = "Validation Failed") String error,
            @Schema(description = "Map of field name to error message") Map<String, String> fields,
            @Schema(example = "2024-06-01T10:00:00") LocalDateTime timestamp
    ) {}
}
