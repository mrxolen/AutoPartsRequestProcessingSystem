package com.autoparts.application;

import com.autoparts.domain.Money;

public record PricingCalculationResult(
        Money sellingPricePerItem,
        Money totalPurchasePrice,
        Money totalSellingPrice,
        Money profit
) {
}
