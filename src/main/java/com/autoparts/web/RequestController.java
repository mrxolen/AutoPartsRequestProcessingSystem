package com.autoparts.web;

import com.autoparts.application.InvalidStatusTransitionException;
import com.autoparts.application.RequestCaseNotFoundException;
import com.autoparts.application.RequestService;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
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

    @GetMapping("/")
    public String home() {
        return "redirect:/requests";
    }

    @GetMapping("/requests")
    public String listRequests(Model model) {
        model.addAttribute("requests", requestService.getAllRequests());
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
        model.addAttribute("requestCase", requestService.getRequestById(id));
        model.addAttribute("nextStatuses", requestService.getAvailableNextStatuses(id));
        model.addAttribute("customerOfferMessage", requestService.generateCustomerOfferMessage(id));
        return "request-details";
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

    @ExceptionHandler(RequestCaseNotFoundException.class)
    public String handleRequestCaseNotFound(RequestCaseNotFoundException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/requests";
    }

    private void addCustomerTypes(Model model) {
        model.addAttribute("customerTypes", CustomerType.values());
    }
}
