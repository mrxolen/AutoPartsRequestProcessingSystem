package com.autoparts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoparts.application.pricing.PricingCalculationResult;
import com.autoparts.application.pricing.PricingStrategyResolver;
import com.autoparts.application.pricing.RegularPricingStrategy;
import com.autoparts.application.pricing.VipPricingStrategy;
import com.autoparts.application.pricing.WalkInPricingStrategy;
import com.autoparts.application.service.PricingService;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    @Test
    void calculatesPricingTotals() {
        PricingService pricingService = new PricingService(new PricingStrategyResolver(List.of(
                new WalkInPricingStrategy(),
                new RegularPricingStrategy(),
                new VipPricingStrategy()
        )));

        PricingCalculationResult result = pricingService.calculate(
                new Money(new BigDecimal("100.00"), "EUR"),
                3,
                CustomerType.REGULAR
        );

        assertThat(result.sellingPricePerItem().getAmount()).isEqualByComparingTo("125.00");
        assertThat(result.totalPurchasePrice().getAmount()).isEqualByComparingTo("300.00");
        assertThat(result.totalSellingPrice().getAmount()).isEqualByComparingTo("375.00");
        assertThat(result.profit().getAmount()).isEqualByComparingTo("75.00");
    }
}
