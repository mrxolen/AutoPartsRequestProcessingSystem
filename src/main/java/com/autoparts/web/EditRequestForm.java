package com.autoparts.web;

import com.autoparts.application.command.UpdateRequestCommand;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditRequestForm {

    @NotBlank
    private String customerName;

    private String phoneNumber;

    @Email
    private String email;

    @NotNull
    private CustomerType customerType;

    @NotBlank
    private String vehicleBrand;

    @NotBlank
    private String vehicleModel;

    @Min(1900)
    @Max(2100)
    private Integer vehicleProductionYear;

    private String vin;

    private String licensePlate;

    public static EditRequestForm from(RequestCase requestCase) {
        EditRequestForm form = new EditRequestForm();
        form.setCustomerName(requestCase.getCustomer().getName());
        form.setPhoneNumber(requestCase.getCustomer().getPhoneNumber());
        form.setEmail(requestCase.getCustomer().getEmail());
        form.setCustomerType(requestCase.getCustomer().getType());
        form.setVehicleBrand(requestCase.getVehicle().getBrand());
        form.setVehicleModel(requestCase.getVehicle().getModel());
        form.setVehicleProductionYear(requestCase.getVehicle().getProductionYear());
        form.setVin(requestCase.getVehicle().getVin());
        form.setLicensePlate(requestCase.getVehicle().getLicensePlate());
        return form;
    }

    public UpdateRequestCommand toCommand() {
        return new UpdateRequestCommand(
                customerName,
                phoneNumber,
                email,
                customerType,
                vehicleBrand,
                vehicleModel,
                vehicleProductionYear,
                vin,
                licensePlate
        );
    }
}
