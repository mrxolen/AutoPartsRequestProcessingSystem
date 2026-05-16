package com.autoparts.web;

import com.autoparts.application.exception.RequestCaseNotFoundException;
import com.autoparts.application.service.ReportService;
import com.autoparts.application.service.RequestService;
import com.autoparts.application.state.InvalidStatusTransitionException;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.SupplierOffer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final ReportService reportService;

    @GetMapping("/")
    public String home() {
        return "redirect:/requests";
    }

    @GetMapping("/requests")
    public String listRequests(
            @RequestParam(defaultValue = "createdDate") String sort,
            @RequestParam(defaultValue = "ALL") String status,
            Model model
    ) {
        model.addAttribute("requests", requestService.getRequests(sort, status));
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("requestStatuses", RequestStatus.values());
        return "requests";
    }

    @GetMapping("/requests/new")
    public String showCreateRequestForm(Model model) {
        model.addAttribute("createRequestForm", new CreateRequestForm());
        addCustomerTypes(model);
        return "create-request";
    }

    @PostMapping("/requests")
    public String createRequest(
            @Valid @ModelAttribute CreateRequestForm createRequestForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addCustomerTypes(model);
            return "create-request";
        }

        RequestCase requestCase = requestService.createRequest(createRequestForm.toCommand());
        redirectAttributes.addFlashAttribute("successMessage", "Request created.");

        return "redirect:/requests/" + requestCase.getId();
    }

    @GetMapping("/requests/{id}")
    public String requestDetails(@PathVariable Long id, Model model) {
        RequestCase requestCase = requestService.getRequestById(id);

        model.addAttribute("requestCase", requestCase);
        model.addAttribute("nextStatuses", requestService.getAvailableNextStatuses(id));
        model.addAttribute("supplierOfferGroups", reportService.groupSupplierOffers(requestCase.getSupplierOffers()));
        model.addAttribute("customerOfferMessage", reportService.generateCustomerOfferMessage(requestCase));
        return "request-details";
    }

    @GetMapping("/requests/{id}/edit")
    public String showEditRequestForm(@PathVariable Long id, Model model) {
        RequestCase requestCase = requestService.getRequestById(id);

        model.addAttribute("requestCase", requestCase);
        model.addAttribute("editRequestForm", EditRequestForm.from(requestCase));
        addCustomerTypes(model);
        return "edit-request";
    }

    @PostMapping("/requests/{id}/edit")
    public String updateRequest(
            @PathVariable Long id,
            @Valid @ModelAttribute EditRequestForm editRequestForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestCase", requestService.getRequestById(id));
            addCustomerTypes(model);
            return "edit-request";
        }

        requestService.updateRequest(id, editRequestForm.toCommand());
        redirectAttributes.addFlashAttribute("successMessage", "Request updated.");

        return "redirect:/requests/" + id;
    }

    @GetMapping("/requests/{id}/parts/new")
    public String showAddRequestedPartForm(@PathVariable Long id, Model model) {
        model.addAttribute("requestCase", requestService.getRequestById(id));
        model.addAttribute("addRequestedPartForm", new AddRequestedPartForm());
        return "add-requested-part";
    }

    @PostMapping("/requests/{id}/parts")
    public String addRequestedPart(
            @PathVariable Long id,
            @Valid @ModelAttribute AddRequestedPartForm addRequestedPartForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestCase", requestService.getRequestById(id));
            return "add-requested-part";
        }

        requestService.addRequestedPart(id, addRequestedPartForm.toCommand());
        redirectAttributes.addFlashAttribute("successMessage", "Requested part added.");

        return "redirect:/requests/" + id;
    }

    @GetMapping("/requests/{id}/requested-parts/{partId}/edit")
    public String showEditRequestedPartForm(@PathVariable Long id, @PathVariable Long partId, Model model) {
        model.addAttribute("requestCase", requestService.getRequestById(id));
        model.addAttribute("editRequestedPartForm", EditRequestedPartForm.from(
                requestService.getRequestedPart(id, partId)
        ));
        model.addAttribute("requestedPartId", partId);
        return "edit-requested-part";
    }

    @PostMapping("/requests/{id}/requested-parts/{partId}/edit")
    public String updateRequestedPart(
            @PathVariable Long id,
            @PathVariable Long partId,
            @Valid @ModelAttribute EditRequestedPartForm editRequestedPartForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestCase", requestService.getRequestById(id));
            model.addAttribute("requestedPartId", partId);
            return "edit-requested-part";
        }

        requestService.updateRequestedPart(id, partId, editRequestedPartForm.toCommand());
        redirectAttributes.addFlashAttribute("successMessage", "Requested part updated.");

        return "redirect:/requests/" + id;
    }

    @GetMapping("/requests/{id}/offers/new")
    public String showAddSupplierOfferForm(@PathVariable Long id, Model model) {
        model.addAttribute("requestCase", requestService.getRequestById(id));
        model.addAttribute("addSupplierOfferForm", new AddSupplierOfferForm());
        return "add-supplier-offer";
    }

    @PostMapping("/requests/{id}/offers")
    public String addSupplierOffer(
            @PathVariable Long id,
            @Valid @ModelAttribute AddSupplierOfferForm addSupplierOfferForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestCase", requestService.getRequestById(id));
            return "add-supplier-offer";
        }

        requestService.addSupplierOffer(id, addSupplierOfferForm.toCommand());
        redirectAttributes.addFlashAttribute("successMessage", "Supplier offer added.");

        return "redirect:/requests/" + id;
    }

    @GetMapping("/requests/{id}/supplier-offers/{offerId}/edit")
    public String showEditSupplierOfferForm(@PathVariable Long id, @PathVariable Long offerId, Model model) {
        SupplierOffer supplierOffer = requestService.getSupplierOffer(id, offerId);

        model.addAttribute("requestCase", requestService.getRequestById(id));
        model.addAttribute("supplierOffer", supplierOffer);
        model.addAttribute("editSupplierOfferForm", EditSupplierOfferForm.from(supplierOffer));
        return "edit-supplier-offer";
    }

    @PostMapping("/requests/{id}/supplier-offers/{offerId}/edit")
    public String updateSupplierOffer(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @Valid @ModelAttribute EditSupplierOfferForm editSupplierOfferForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestCase", requestService.getRequestById(id));
            model.addAttribute("supplierOffer", requestService.getSupplierOffer(id, offerId));
            return "edit-supplier-offer";
        }

        requestService.updateSupplierOffer(id, offerId, editSupplierOfferForm.toCommand());
        redirectAttributes.addFlashAttribute("successMessage", "Supplier offer updated.");

        return "redirect:/requests/" + id;
    }

    @PostMapping("/requests/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus nextStatus,
            RedirectAttributes redirectAttributes
    ) {
        try {
            requestService.changeRequestStatus(id, nextStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Request status changed.");
        } catch (InvalidStatusTransitionException | IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/requests/" + id;
    }

    @PostMapping("/requests/{id}/delete")
    public String deleteRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        requestService.deleteRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Request deleted.");
        return "redirect:/requests";
    }

    @ExceptionHandler(RequestCaseNotFoundException.class)
    public String handleRequestCaseNotFound(RequestCaseNotFoundException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/requests";
    }

    private void addCustomerTypes(Model model) {
        model.addAttribute("customerTypes", CustomerType.values());
    }
}
