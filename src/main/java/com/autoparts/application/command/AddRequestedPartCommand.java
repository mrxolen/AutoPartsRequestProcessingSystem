package com.autoparts.application.command;

import jakarta.validation.constraints.NotBlank;

public record AddRequestedPartCommand(
        @NotBlank String partName,
        String note
) {
}
