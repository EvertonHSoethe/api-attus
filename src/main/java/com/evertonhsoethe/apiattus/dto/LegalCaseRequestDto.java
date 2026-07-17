package com.evertonhsoethe.apiattus.dto;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LegalCaseRequestDto(

        @NotBlank(message = "Case number is required")
        @Size(max = 50, message = "Case number must not exceed 50 characters")
        String caseNumber,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Case type is required")
        CaseTypeEnum type,

        CaseStatusEnum status,

        @NotBlank(message = "Plaintiff is required")
        @Size(max = 150, message = "Plaintiff must not exceed 150 characters")
        String plaintiff,

        @NotBlank(message = "Defendant is required")
        @Size(max = 150, message = "Defendant must not exceed 150 characters")
        String defendant,

        @NotNull(message = "Filing date is required")
        LocalDate filingDate,

        LocalDate closingDate
) {}
