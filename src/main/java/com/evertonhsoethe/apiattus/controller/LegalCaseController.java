package com.evertonhsoethe.apiattus.controller;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import com.evertonhsoethe.apiattus.dto.LegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.LegalCaseResponseDto;
import com.evertonhsoethe.apiattus.dto.PatchLegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.UpdateStatusRequestDto;
import com.evertonhsoethe.apiattus.exception.GlobalExceptionHandler.ErrorResponse;
import com.evertonhsoethe.apiattus.exception.GlobalExceptionHandler.ValidationErrorResponse;
import com.evertonhsoethe.apiattus.service.LegalCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Legal Cases", description = "Endpoints for managing legal cases")
@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class LegalCaseController {

    private final LegalCaseService service;

    @Operation(summary = "Create a legal case")
    @ApiResponse(responseCode = "201", description = "Case created",
            content = @Content(schema = @Schema(implementation = LegalCaseResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Case number already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<LegalCaseResponseDto> create(@Valid @RequestBody LegalCaseRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "List legal cases", description = "Returns all cases. Optionally filter by status or type.")
    @ApiResponse(responseCode = "200", description = "Cases returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LegalCaseResponseDto.class))))
    @GetMapping
    public ResponseEntity<List<LegalCaseResponseDto>> list(
            @Parameter(description = "Filter by case status") @RequestParam(required = false) CaseStatusEnum status,
            @Parameter(description = "Filter by case type")  @RequestParam(required = false) CaseTypeEnum type) {

        if (status != null) return ResponseEntity.ok(service.findByStatus(status));
        if (type != null)   return ResponseEntity.ok(service.findByType(type));
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Get a legal case by ID")
    @ApiResponse(responseCode = "200", description = "Case found",
            content = @Content(schema = @Schema(implementation = LegalCaseResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Case not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<LegalCaseResponseDto> findById(
            @Parameter(description = "Case ID") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Get a legal case by case number")
    @ApiResponse(responseCode = "200", description = "Case found",
            content = @Content(schema = @Schema(implementation = LegalCaseResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Case not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/number/{caseNumber}")
    public ResponseEntity<LegalCaseResponseDto> findByCaseNumber(
            @Parameter(description = "Case number") @PathVariable String caseNumber) {
        return ResponseEntity.ok(service.findByCaseNumber(caseNumber));
    }

    @Operation(summary = "Update a legal case")
    @ApiResponse(responseCode = "200", description = "Case updated",
            content = @Content(schema = @Schema(implementation = LegalCaseResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Case not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Case number already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<LegalCaseResponseDto> update(
            @Parameter(description = "Case ID") @PathVariable Long id,
            @Valid @RequestBody LegalCaseRequestDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Partially update a legal case", description = "Only the fields present in the request body are updated. Omitted fields retain their current values.")
    @ApiResponse(responseCode = "200", description = "Case updated",
            content = @Content(schema = @Schema(implementation = LegalCaseResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Case not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Case number already in use",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}")
    public ResponseEntity<LegalCaseResponseDto> patch(
            @Parameter(description = "Case ID") @PathVariable Long id,
            @Valid @RequestBody PatchLegalCaseRequestDto request) {
        return ResponseEntity.ok(service.patch(id, request));
    }

    @Operation(summary = "Update the status of a legal case")
    @ApiResponse(responseCode = "200", description = "Status updated",
            content = @Content(schema = @Schema(implementation = LegalCaseResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Case not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}/status")
    public ResponseEntity<LegalCaseResponseDto> updateStatus(
            @Parameter(description = "Case ID") @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequestDto request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @Operation(summary = "Delete a legal case")
    @ApiResponse(responseCode = "204", description = "Case deleted")
    @ApiResponse(responseCode = "404", description = "Case not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Case ID") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
