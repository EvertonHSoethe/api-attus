package com.evertonhsoethe.apiattus.dto;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequestDto(
        @NotNull(message = "Status is required")
        CaseStatusEnum status
) {}
