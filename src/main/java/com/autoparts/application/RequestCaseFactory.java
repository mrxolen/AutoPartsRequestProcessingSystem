package com.autoparts.application;

import com.autoparts.domain.Customer;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class RequestCaseFactory {

    public RequestCase create(CreateRequestCommand command) {
        if (command.customerType() == null) {
            throw new IllegalArgumentException("Customer type is required.");
        }

        return switch (command.customerType()) {
            case WALK_IN -> createRequestCase(command, CustomerType.WALK_IN);
            case REGULAR -> createRequestCase(command, CustomerType.REGULAR);
            case VIP -> createRequestCase(command, CustomerType.VIP);
        };
    }

    private RequestCase createRequestCase(CreateRequestCommand command, CustomerType customerType) {
        Customer customer = new Customer();
        customer.setName(command.customerName());
        customer.setPhoneNumber(command.phoneNumber());
        customer.setEmail(command.email());
        customer.setType(customerType);

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(command.vehicleBrand());
        vehicle.setModel(command.vehicleModel());
        vehicle.setProductionYear(command.vehicleProductionYear());
        vehicle.setVin(command.vin());

        RequestCase requestCase = new RequestCase();
        requestCase.setCustomer(customer);
        requestCase.setVehicle(vehicle);
        requestCase.setStatus(RequestStatus.NEW);

        return requestCase;
    }
}
