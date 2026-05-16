package com.autoparts.application.service;

import com.autoparts.application.command.AddRequestedPartCommand;
import com.autoparts.application.command.AddSupplierOfferCommand;
import com.autoparts.application.command.CreateRequestCommand;
import com.autoparts.application.exception.RequestCaseNotFoundException;
import com.autoparts.application.factory.RequestCaseFactory;
import com.autoparts.application.observer.RequestStatusObserver;
import com.autoparts.application.pricing.PricingCalculationResult;
import com.autoparts.application.state.StatusTransitionService;
import com.autoparts.domain.Money;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.RequestedPart;
import com.autoparts.domain.SupplierOffer;
import com.autoparts.infrastructure.CustomerRepository;
import com.autoparts.infrastructure.RequestCaseRepository;
import com.autoparts.infrastructure.VehicleRepository;
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
