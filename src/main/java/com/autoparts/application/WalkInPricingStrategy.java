package com.autoparts.application;

import com.autoparts.domain.CustomerType;
import com.autoparts.domain.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class WalkInPricingStrategy implements PricingStrategy {

    private static final BigDecimal FIXED_MARKUP = new BigDecimal("15.00");

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.WALK_IN;
    }

    @Override
    public Money calculateSellingPricePerItem(Money purchasePricePerItem) {
        BigDecimal sellingAmount = purchasePricePerItem.getAmount()
                .add(FIXED_MARKUP)
                .setScale(2, RoundingMode.HALF_UP);

        return new Money(sellingAmount, purchasePricePerItem.getCurrency());
    }
}
