package com.autoparts.web;

import com.autoparts.application.AddRequestedPartCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRequestedPartForm {

    @NotBlank
    private String partName;

    @Size(max = 1000)
    private String note;

    public AddRequestedPartCommand toCommand() {
        return new AddRequestedPartCommand(partName, note);
    }
}
