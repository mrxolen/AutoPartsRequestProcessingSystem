package com.autoparts.application;

import com.autoparts.domain.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRequestCommand(
        @NotBlank String customerName,
        String phoneNumber,
        String email,
        @NotNull CustomerType customerType,
        @NotBlank String vehicleBrand,
        @NotBlank String vehicleModel,
        Integer vehicleProductionYear,
        String vin
) {
}
