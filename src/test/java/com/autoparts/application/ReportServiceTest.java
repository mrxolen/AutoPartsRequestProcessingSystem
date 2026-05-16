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
                   Quantity: 2
                   Options:
                   - BREMSI: 40.00 EUR each
                
                2. Front brake pads
                   Code: 7M3 698 151 B
                   Options:
                   - DON: 25.00 EUR
                
                Total: 105.00 EUR""");
    }

    @Test
    void groupsAlternativeSupplierOffersAndCalculatesPriceRange() {
        RequestCase requestCase = new RequestCase();

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("Audi");
        vehicle.setModel("A6");
        vehicle.setProductionYear(2008);
        vehicle.setVin("1341");
        requestCase.setVehicle(vehicle);

        requestCase.getSupplierOffers().add(supplierOffer(
                "FDB1093",
                "Brake pads",
                1,
                "27.45",
                "Ferodo"
        ));
        requestCase.getSupplierOffers().add(supplierOffer(
                "FDB1093",
                "Brake pads",
                1,
                "40.23",
                "ATE"
        ));
        requestCase.getSupplierOffers().add(supplierOffer(
                "142 1020",
                "Brake disc",
                2,
                "57.23",
                "CAR"
        ));

        String message = reportService.generateCustomerOfferMessage(requestCase);

        assertThat(message).contains("""
                1. Brake pads
                   Code: FDB1093
                   Options:
                   - Ferodo: 27.45 EUR
                   - ATE: 40.23 EUR
                """);
        assertThat(message).contains("""
                2. Brake disc
                   Code: 142 1020
                   Quantity: 2
                   Options:
                   - CAR: 57.23 EUR each
                """);
        assertThat(message).containsOnlyOnce("Brake pads");
        assertThat(message).contains("""
                Total:
                Depends on selected options.
                Price range: 141.91 EUR - 154.69 EUR""");
        assertThat(message).doesNotContain("182.14 EUR");
    }

    @Test
    void groupsOffersUsingTrimmedCaseInsensitivePartIdentity() {
        RequestCase requestCase = new RequestCase();

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("Audi");
        vehicle.setModel("A6");
        requestCase.setVehicle(vehicle);

        requestCase.getSupplierOffers().add(supplierOffer(
                "FDB1093",
                "Brake pads",
                1,
                "27.45",
                "Ferodo"
        ));
        requestCase.getSupplierOffers().add(supplierOffer(
                " fdb1093 ",
                " brake pads ",
                1,
                "40.23",
                "ATE"
        ));

        assertThat(reportService.groupSupplierOffers(requestCase.getSupplierOffers())).hasSize(1);
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
