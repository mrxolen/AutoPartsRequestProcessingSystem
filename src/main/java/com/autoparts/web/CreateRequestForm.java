package com.autoparts.web;

import com.autoparts.application.command.CreateRequestCommand;
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
public class CreateRequestForm {

    @NotBlank
    private String customerName;

    private String phoneNumber;

    @Email
    private String email;

    @NotNull
    private CustomerType customerType = CustomerType.WALK_IN;

    @NotBlank
    private String vehicleBrand;

    @NotBlank
    private String vehicleModel;

    @Min(1900)
    @Max(2100)
    private Integer vehicleProductionYear;

    private String vin;

    private String licensePlate;

    public CreateRequestCommand toCommand() {
        return new CreateRequestCommand(
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
