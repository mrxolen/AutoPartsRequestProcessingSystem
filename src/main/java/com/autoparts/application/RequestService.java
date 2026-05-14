package com.autoparts.application;

import com.autoparts.domain.Money;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.RequestedPart;
import com.autoparts.domain.SupplierOffer;
import com.autoparts.infrastructure.CustomerRepository;
import com.autoparts.infrastructure.RequestCaseRepository;
import com.autoparts.infrastructure.VehicleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestCaseRepository requestCaseRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final RequestCaseFactory requestCaseFactory;
    private final PricingService pricingService;
    private final StatusTransitionService statusTransitionService;
    private final List<RequestStatusObserver> requestStatusObservers;

    @Transactional
    public RequestCase createRequest(CreateRequestCommand command) {
        RequestCase requestCase = requestCaseFactory.create(command);

        customerRepository.save(requestCase.getCustomer());
        vehicleRepository.save(requestCase.getVehicle());

        return requestCaseRepository.save(requestCase);
    }

    @Transactional(readOnly = true)
    public List<RequestCase> getAllRequests() {
        return requestCaseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RequestCase getRequestById(Long requestCaseId) {
        return findRequestCase(requestCaseId);
    }

    @Transactional(readOnly = true)
    public List<RequestStatus> getAvailableNextStatuses(Long requestCaseId) {
        RequestCase requestCase = findRequestCase(requestCaseId);

        return statusTransitionService.getAllowedNextStatuses(requestCase.getStatus());
    }

    @Transactional(readOnly = true)
    public String generateCustomerOfferMessage(Long requestCaseId) {
        RequestCase requestCase = findRequestCase(requestCaseId);

        if (requestCase.getSupplierOffers().isEmpty()) {
            return "No supplier offers have been added yet.";
        }

        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(requestCase.getCustomer().getName()).append(",\n\n");
        message.append("We have prepared an offer for your ")
                .append(requestCase.getVehicle().getBrand()).append(" ")
                .append(requestCase.getVehicle().getModel()).append(".\n\n");

        BigDecimal totalSellingPrice = BigDecimal.ZERO;
        String currency = requestCase.getSupplierOffers().getFirst().getSellingPrice().getCurrency();

        for (SupplierOffer supplierOffer : requestCase.getSupplierOffers()) {
            BigDecimal lineTotal = supplierOffer.getSellingPrice().getAmount()
                    .multiply(BigDecimal.valueOf(supplierOffer.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            totalSellingPrice = totalSellingPrice.add(lineTotal);

            message.append("- ")
                    .append(supplierOffer.getQuantity()).append(" x ")
                    .append(supplierOffer.getBrandName()).append(" ")
                    .append(supplierOffer.getPartName()).append(" (")
                    .append(supplierOffer.getPartCode()).append("): ")
                    .append(lineTotal).append(" ")
                    .append(supplierOffer.getSellingPrice().getCurrency())
                    .append("\n");
        }

        message.append("\nTotal selling price: ")
                .append(totalSellingPrice.setScale(2, RoundingMode.HALF_UP))
                .append(" ")
                .append(currency)
                .append("\n\nPlease contact us if you want to accept this offer.");

        return message.toString();
    }

    @Transactional
    public RequestCase addRequestedPart(Long requestCaseId, AddRequestedPartCommand command) {
        RequestCase requestCase = findRequestCase(requestCaseId);

        RequestedPart requestedPart = new RequestedPart();
        requestedPart.setPartName(command.partName());
        requestedPart.setNote(command.note());
        requestedPart.setRequestCase(requestCase);

        requestCase.getRequestedParts().add(requestedPart);

        return requestCaseRepository.save(requestCase);
    }

    @Transactional
    public RequestCase addSupplierOffer(Long requestCaseId, AddSupplierOfferCommand command) {
        RequestCase requestCase = findRequestCase(requestCaseId);

        Money purchasePrice = new Money(command.purchasePriceAmount(), command.currency());
        PricingCalculationResult pricing = pricingService.calculate(
                purchasePrice,
                command.quantity(),
                requestCase.getCustomer().getType()
        );

        SupplierOffer supplierOffer = new SupplierOffer();
        supplierOffer.setPartCode(command.partCode());
        supplierOffer.setPartName(command.partName());
        supplierOffer.setQuantity(command.quantity());
        supplierOffer.setPurchasePrice(purchasePrice);
        supplierOffer.setSellingPrice(pricing.sellingPricePerItem());
        supplierOffer.setSupplierName(command.supplierName());
        supplierOffer.setBrandName(command.brandName());
        supplierOffer.setRequestCase(requestCase);

        requestCase.getSupplierOffers().add(supplierOffer);

        return requestCaseRepository.save(requestCase);
    }

    @Transactional
    public RequestCase changeRequestStatus(Long requestCaseId, RequestStatus nextStatus) {
        RequestCase requestCase = findRequestCase(requestCaseId);
        RequestStatus oldStatus = requestCase.getStatus();

        statusTransitionService.changeStatus(requestCase, nextStatus);
        notifyStatusObservers(requestCase, oldStatus, nextStatus);

        return requestCaseRepository.save(requestCase);
    }

    @Transactional
    public RequestCase saveChanges(RequestCase requestCase) {
        return requestCaseRepository.save(requestCase);
    }

    private RequestCase findRequestCase(Long requestCaseId) {
        return requestCaseRepository.findById(requestCaseId)
                .orElseThrow(() -> new RequestCaseNotFoundException(requestCaseId));
    }

    private void notifyStatusObservers(RequestCase requestCase, RequestStatus oldStatus, RequestStatus newStatus) {
        for (RequestStatusObserver observer : requestStatusObservers) {
            observer.onStatusChanged(requestCase, oldStatus, newStatus);
        }
    }
}
