package com.autoparts.application;

import com.autoparts.domain.CustomerType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PricingStrategyResolver {

    private final Map<CustomerType, PricingStrategy> strategies = new EnumMap<>(CustomerType.class);

    public PricingStrategyResolver(List<PricingStrategy> pricingStrategies) {
        for (PricingStrategy pricingStrategy : pricingStrategies) {
            strategies.put(pricingStrategy.getCustomerType(), pricingStrategy);
        }
    }

    public PricingStrategy resolve(CustomerType customerType) {
        PricingStrategy pricingStrategy = strategies.get(customerType);

        if (pricingStrategy == null) {
            throw new IllegalArgumentException("No pricing strategy found for customer type: " + customerType);
        }

        return pricingStrategy;
    }
}
