package com.autoparts.web;

import com.autoparts.application.command.UpdateSupplierOfferCommand;
import com.autoparts.domain.SupplierOffer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditSupplierOfferForm {

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
    private String supplierName;

    @NotBlank
    private String brandName;

    public static EditSupplierOfferForm from(SupplierOffer supplierOffer) {
        EditSupplierOfferForm form = new EditSupplierOfferForm();
        form.setPartCode(supplierOffer.getPartCode());
        form.setPartName(supplierOffer.getPartName());
        form.setQuantity(supplierOffer.getQuantity());
        form.setPurchasePriceAmount(supplierOffer.getPurchasePrice().getAmount());
        form.setSupplierName(supplierOffer.getSupplierName());
        form.setBrandName(supplierOffer.getBrandName());
        return form;
    }

    public UpdateSupplierOfferCommand toCommand() {
        return new UpdateSupplierOfferCommand(
                partCode,
                partName,
                quantity,
                purchasePriceAmount,
                supplierName,
                brandName
        );
    }
}
