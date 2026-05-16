package com.autoparts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoparts.application.pricing.PricingStrategy;
import com.autoparts.application.pricing.RegularPricingStrategy;
import com.autoparts.application.pricing.VipPricingStrategy;
import com.autoparts.application.pricing.WalkInPricingStrategy;
import com.autoparts.domain.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PricingStrategyTest {

    @Test
    void walkInCustomerGetsFixedMarkup() {
        PricingStrategy strategy = new WalkInPricingStrategy();

        Money result = strategy.calculateSellingPricePerItem(new Money(new BigDecimal("100.00"), "EUR"));

        assertThat(result.getAmount()).isEqualByComparingTo("115.00");
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void regularCustomerGetsTwentyFivePercentMarkup() {
        PricingStrategy strategy = new RegularPricingStrategy();

        Money result = strategy.calculateSellingPricePerItem(new Money(new BigDecimal("100.00"), "EUR"));

        assertThat(result.getAmount()).isEqualByComparingTo("125.00");
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void vipCustomerGetsFifteenPercentMarkup() {
        PricingStrategy strategy = new VipPricingStrategy();

        Money result = strategy.calculateSellingPricePerItem(new Money(new BigDecimal("100.00"), "EUR"));

        assertThat(result.getAmount()).isEqualByComparingTo("115.00");
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }
}
