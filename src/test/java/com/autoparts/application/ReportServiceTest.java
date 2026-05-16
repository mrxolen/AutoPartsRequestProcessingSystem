package com.autoparts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoparts.application.service.ReportService;
import com.autoparts.domain.Money;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.SupplierOffer;
import com.autoparts.domain.Vehicle;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

    private final ReportService reportService = new ReportService();

    @Test
    void generatesCustomerReadyOfferMessage() {
        RequestCase requestCase = new RequestCase();

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("SEAT");
        vehicle.setModel("ALHAMBRA");
        vehicle.setProductionYear(2007);
        vehicle.setVin("VSSZZZ7MZ8V505695");
        requestCase.setVehicle(vehicle);

        requestCase.getSupplierOffers().add(supplierOffer(
                "7M3 615 301 A",
                "Front brake discs",
                2,
                "40.00",
                "BREMSI"
        ));
        requestCase.getSupplierOffers().add(supplierOffer(
                "7M3 698 151 B",
                "Front brake pads",
                1,
                "25.00",
                "DON"
        ));

        String message = reportService.generateCustomerOfferMessage(requestCase);

        assertThat(message).isEqualTo("""
                Vehicle: SEAT ALHAMBRA 2007
                VIN: VSSZZZ7MZ8V505695
                
                Offer:
                1. Front brake discs
                   Code: 7M3 615 301 A
                   Brand: BREMSI
                   Quantity: 2
                   Price: 40.00 EUR each
                
                2. Front brake pads
                   Code: 7M3 698 151 B
                   Brand: DON
                   Quantity: 1
                   Price: 25.00 EUR
                
                Total: 105.00 EUR""");
    }

    private SupplierOffer supplierOffer(String code, String name, int quantity, String price, String brand) {
        SupplierOffer supplierOffer = new SupplierOffer();
        supplierOffer.setPartCode(code);
        supplierOffer.setPartName(name);
        supplierOffer.setQuantity(quantity);
        supplierOffer.setSellingPrice(new Money(new BigDecimal(price), "EUR"));
        supplierOffer.setBrandName(brand);
        return supplierOffer;
    }
}
