package com.autoparts.application.service;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.SupplierOffer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        message.append("Offer:\n\n");

        List<SupplierOfferGroup> groups = groupSupplierOffers(requestCase.getSupplierOffers());
        BigDecimal minimumTotal = BigDecimal.ZERO;
        BigDecimal maximumTotal = BigDecimal.ZERO;
        String currency = requestCase.getSupplierOffers().getFirst().getSellingPrice().getCurrency();

        for (int index = 0; index < groups.size(); index++) {
            SupplierOfferGroup group = groups.get(index);
            minimumTotal = minimumTotal.add(calculateMinimumGroupTotal(group));
            maximumTotal = maximumTotal.add(calculateMaximumGroupTotal(group));

            message.append(index + 1).append(". ").append(group.getPartName()).append("\n");
            message.append("   Code: ").append(group.getPartCode()).append("\n");

            Integer quantity = getCommonQuantity(group);
            if (quantity != null && quantity > 1) {
                message.append("   Quantity: ").append(quantity).append("\n");
            }

            message.append("   Options:\n");

            for (SupplierOffer offer : group.getOffers()) {
                message.append("   - ").append(offer.getBrandName()).append(": ")
                        .append(formatAmount(offer.getSellingPrice().getAmount())).append(" ")
                        .append(offer.getSellingPrice().getCurrency());

                if (offer.getQuantity() > 1) {
                    message.append(" each");
                }

                message.append("\n");
            }

            message.append("\n");
        }

        if (minimumTotal.compareTo(maximumTotal) == 0) {
            message.append("Total: ")
                    .append(formatAmount(minimumTotal))
                    .append(" ")
                    .append(currency);
        } else {
            message.append("Total:\n");
            message.append("Depends on selected options.\n");
            message.append("Price range: ")
                    .append(formatAmount(minimumTotal))
                    .append(" ")
                    .append(currency)
                    .append(" - ")
                    .append(formatAmount(maximumTotal))
                    .append(" ")
                    .append(currency);
        }

        return message.toString();
    }

    public List<SupplierOfferGroup> groupSupplierOffers(List<SupplierOffer> supplierOffers) {
        Map<String, SupplierOfferGroup> groups = new LinkedHashMap<>();

        for (SupplierOffer offer : supplierOffers) {
            String normalizedPartCode = normalize(offer.getPartCode());
            String normalizedPartName = normalize(offer.getPartName());
            String groupKey = normalizedPartCode + "|" + normalizedPartName;

            SupplierOfferGroup group = groups.computeIfAbsent(groupKey, ignored -> new SupplierOfferGroup(
                    cleanDisplayValue(offer.getPartCode()),
                    cleanDisplayValue(offer.getPartName())
            ));
            group.addOffer(offer);
        }

        return new ArrayList<>(groups.values());
    }

    private BigDecimal calculateLineTotal(SupplierOffer offer) {
        return offer.getSellingPrice().getAmount()
                .multiply(BigDecimal.valueOf(offer.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMinimumGroupTotal(SupplierOfferGroup group) {
        return group.getOffers().stream()
                .map(this::calculateLineTotal)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateMaximumGroupTotal(SupplierOfferGroup group) {
        return group.getOffers().stream()
                .map(this::calculateLineTotal)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private Integer getCommonQuantity(SupplierOfferGroup group) {
        Integer firstQuantity = group.getOffers().getFirst().getQuantity();

        boolean allQuantitiesMatch = group.getOffers().stream()
                .allMatch(offer -> firstQuantity.equals(offer.getQuantity()));

        if (!allQuantitiesMatch) {
            return null;
        }

        return firstQuantity;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanDisplayValue(String value) {
        return value.trim();
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
