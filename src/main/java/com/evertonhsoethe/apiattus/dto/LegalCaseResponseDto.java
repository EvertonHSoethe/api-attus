package com.evertonhsoethe.apiattus.dto;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Legal case response payload")
public record LegalCaseResponseDto(

        @Schema(description = "Unique identifier", example = "1")
        Long id,

        @Schema(description = "Unique case number", example = "0001234-12.2024.8.26.0100")
        String caseNumber,

        @Schema(description = "Case title", example = "Smith v. Acme Corp.")
        String title,

        @Schema(description = "Detailed description of the case")
        String description,

        @Schema(description = "Current status", example = "OPEN")
        CaseStatusEnum status,

        @Schema(description = "Case type", example = "CIVIL")
        CaseTypeEnum type,

        @Schema(description = "Plaintiff name", example = "John Smith")
        String plaintiff,

        @Schema(description = "Defendant name", example = "Acme Corp.")
        String defendant,

        @Schema(description = "Date the case was filed", example = "2024-01-15")
        LocalDate filingDate,

        @Schema(description = "Date the case was closed, if applicable", example = "2025-03-10")
        LocalDate closingDate,

        @Schema(description = "Record creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
