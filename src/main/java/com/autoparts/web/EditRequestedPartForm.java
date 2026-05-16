package com.autoparts.web;

import com.autoparts.application.command.UpdateRequestedPartCommand;
import com.autoparts.domain.RequestedPart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditRequestedPartForm {

    @NotBlank
    private String partName;

    @Size(max = 1000)
    private String note;

    public static EditRequestedPartForm from(RequestedPart requestedPart) {
        EditRequestedPartForm form = new EditRequestedPartForm();
        form.setPartName(requestedPart.getPartName());
        form.setNote(requestedPart.getNote());
        return form;
    }

    public UpdateRequestedPartCommand toCommand() {
        return new UpdateRequestedPartCommand(partName, note);
    }
}
