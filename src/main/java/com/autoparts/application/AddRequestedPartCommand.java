package com.autoparts.application;

import jakarta.validation.constraints.NotBlank;

public record AddRequestedPartCommand(
        @NotBlank String partName,
        String note
) {
}
