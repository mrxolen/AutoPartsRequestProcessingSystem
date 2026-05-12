package com.autoparts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoparts.domain.CustomerType;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import org.junit.jupiter.api.Test;

class RequestCaseFactoryTest {

    private final RequestCaseFactory requestCaseFactory = new RequestCaseFactory();

    @Test
    void createsVipRequestCase() {
        CreateRequestCommand command = new CreateRequestCommand(
                "polrig",
                "12345678",
                "polrig@example.com",
                CustomerType.VIP,
                "SEAT",
                "ALHAMBRA",
                2007,
                "VSSZZZ7MZ8V505695"
        );

        RequestCase requestCase = requestCaseFactory.create(command);

        assertThat(requestCase.getStatus()).isEqualTo(RequestStatus.NEW);
        assertThat(requestCase.getCustomer().getName()).isEqualTo("polrig");
        assertThat(requestCase.getCustomer().getType()).isEqualTo(CustomerType.VIP);
        assertThat(requestCase.getVehicle().getBrand()).isEqualTo("SEAT");
        assertThat(requestCase.getVehicle().getModel()).isEqualTo("ALHAMBRA");
        assertThat(requestCase.getVehicle().getProductionYear()).isEqualTo(2007);
        assertThat(requestCase.getVehicle().getVin()).isEqualTo("VSSZZZ7MZ8V505695");
    }
}
