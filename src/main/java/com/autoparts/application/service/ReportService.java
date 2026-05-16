package com.autoparts.application.service;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.SupplierOffer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public String generateCustomerOfferMessage(RequestCase requestCase) {
        if (requestCase.getSupplierOffers().isEmpty()) {
            return "No supplier offers have been added yet.";
        }

        StringBuilder message = new StringBuilder();
        message.append("Vehicle: ")
                .append(requestCase.getVehicle().getBrand()).append(" ")
                .append(requestCase.getVehicle().getModel());

        if (requestCase.getVehicle().getProductionYear() != null) {
            message.append(" ").append(requestCase.getVehicle().getProductionYear());
        }

        message.append("\n");
        message.append("VIN: ").append(valueOrDash(requestCase.getVehicle().getVin())).append("\n\n");
        message.append("Offer:\n");

        BigDecimal total = BigDecimal.ZERO;
        String currency = requestCase.getSupplierOffers().getFirst().getSellingPrice().getCurrency();

        for (int index = 0; index < requestCase.getSupplierOffers().size(); index++) {
            SupplierOffer offer = requestCase.getSupplierOffers().get(index);
            BigDecimal lineTotal = calculateLineTotal(offer);
            total = total.add(lineTotal);

            message.append(index + 1).append(". ").append(offer.getPartName()).append("\n");
            message.append("   Code: ").append(offer.getPartCode()).append("\n");
            message.append("   Brand: ").append(offer.getBrandName()).append("\n");
            message.append("   Quantity: ").append(offer.getQuantity()).append("\n");
            message.append("   Price: ")
                    .append(formatAmount(offer.getSellingPrice().getAmount())).append(" ")
                    .append(offer.getSellingPrice().getCurrency());

            if (offer.getQuantity() > 1) {
                message.append(" each");
            }

            message.append("\n\n");
        }

        message.append("Total: ")
                .append(formatAmount(total))
                .append(" ")
                .append(currency);

        return message.toString();
    }

    private BigDecimal calculateLineTotal(SupplierOffer offer) {
        return offer.getSellingPrice().getAmount()
                .multiply(BigDecimal.valueOf(offer.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }
}
