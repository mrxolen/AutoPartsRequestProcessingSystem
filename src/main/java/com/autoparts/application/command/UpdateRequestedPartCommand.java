package com.autoparts.application.command;

import jakarta.validation.constraints.NotBlank;

public record UpdateRequestedPartCommand(
        @NotBlank String partName,
        String note
) {
}
