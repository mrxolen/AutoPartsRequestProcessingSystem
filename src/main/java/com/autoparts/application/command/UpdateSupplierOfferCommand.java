package com.autoparts.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateSupplierOfferCommand(
        @NotBlank String partCode,
        @NotBlank String partName,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal purchasePriceAmount,
        @NotBlank String supplierName,
        @NotBlank String brandName
) {
}
