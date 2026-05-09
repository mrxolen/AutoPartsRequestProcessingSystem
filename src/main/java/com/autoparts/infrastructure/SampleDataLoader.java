package com.autoparts.infrastructure;

import com.autoparts.domain.Customer;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.RequestedPart;
import com.autoparts.domain.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SampleDataLoader implements CommandLineRunner {

    private final RequestCaseRepository requestCaseRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (requestCaseRepository.count() > 0) {
            return;
        }

        Customer customer = new Customer();
        customer.setName("polrig");
        customer.setType(CustomerType.REGULAR);

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("SEAT");
        vehicle.setModel("ALHAMBRA");
        vehicle.setProductionYear(2007);
        vehicle.setVin("VSSZZZ7MZ8V505695");

        customerRepository.save(customer);
        vehicleRepository.save(vehicle);

        RequestCase requestCase = new RequestCase();
        requestCase.setCustomer(customer);
        requestCase.setVehicle(vehicle);
        requestCase.setStatus(RequestStatus.NEW);

        addRequestedPart(requestCase, "front brake discs");
        addRequestedPart(requestCase, "front brake pads");
        addRequestedPart(requestCase, "rear springs");

        requestCaseRepository.save(requestCase);
    }

    private void addRequestedPart(RequestCase requestCase, String partName) {
        RequestedPart requestedPart = new RequestedPart();
        requestedPart.setPartName(partName);
        requestedPart.setRequestCase(requestCase);
        requestCase.getRequestedParts().add(requestedPart);
    }
}
