package com.evertonhsoethe.apiattus.dto;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatchLegalCaseRequestDto(

        @Size(max = 50, message = "Case number must not exceed 50 characters")
        String caseNumber,

        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        CaseTypeEnum type,

        CaseStatusEnum status,

        @Size(max = 150, message = "Plaintiff must not exceed 150 characters")
        String plaintiff,

        @Size(max = 150, message = "Defendant must not exceed 150 characters")
        String defendant,

        LocalDate filingDate,

        LocalDate closingDate
) {}
