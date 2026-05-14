package com.autoparts.web;

import com.autoparts.application.AddSupplierOfferCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddSupplierOfferForm {

    @NotBlank
    private String partCode;

    @NotBlank
    private String partName;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @Positive
    private BigDecimal purchasePriceAmount;

    @NotBlank
    private String currency = "EUR";

    @NotBlank
    private String supplierName;

    @NotBlank
    private String brandName;

    public AddSupplierOfferCommand toCommand() {
        return new AddSupplierOfferCommand(
                partCode,
                partName,
                quantity,
                purchasePriceAmount,
                currency,
                supplierName,
                brandName
        );
    }
}
