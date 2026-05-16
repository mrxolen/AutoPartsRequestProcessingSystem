package com.autoparts.application.pricing;

import com.autoparts.domain.CustomerType;
import com.autoparts.domain.Money;

public interface PricingStrategy {

    CustomerType getCustomerType();

    Money calculateSellingPricePerItem(Money purchasePricePerItem);
}
