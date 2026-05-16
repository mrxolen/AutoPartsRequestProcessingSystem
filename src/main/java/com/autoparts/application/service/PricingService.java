package com.autoparts.application.service;

import com.autoparts.application.pricing.PricingCalculationResult;
import com.autoparts.application.pricing.PricingStrategy;
import com.autoparts.application.pricing.PricingStrategyResolver;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingStrategyResolver pricingStrategyResolver;

    public PricingCalculationResult calculate(Money purchasePricePerItem, int quantity, CustomerType customerType) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        PricingStrategy pricingStrategy = pricingStrategyResolver.resolve(customerType);
        Money sellingPricePerItem = pricingStrategy.calculateSellingPricePerItem(purchasePricePerItem);

        Money totalPurchasePrice = multiply(purchasePricePerItem, quantity);
        Money totalSellingPrice = multiply(sellingPricePerItem, quantity);
        Money profit = subtract(totalSellingPrice, totalPurchasePrice);

        return new PricingCalculationResult(
                sellingPricePerItem,
                totalPurchasePrice,
                totalSellingPrice,
                profit
        );
    }

    private Money multiply(Money money, int quantity) {
        BigDecimal amount = money.getAmount()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        return new Money(amount, money.getCurrency());
    }

    private Money subtract(Money left, Money right) {
        BigDecimal amount = left.getAmount()
                .subtract(right.getAmount())
                .setScale(2, RoundingMode.HALF_UP);

        return new Money(amount, left.getCurrency());
    }
}
