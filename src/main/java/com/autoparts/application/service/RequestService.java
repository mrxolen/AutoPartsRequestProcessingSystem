package com.autoparts.application.service;

import com.autoparts.application.command.AddRequestedPartCommand;
import com.autoparts.application.command.AddSupplierOfferCommand;
import com.autoparts.application.command.CreateRequestCommand;
import com.autoparts.application.command.UpdateRequestCommand;
import com.autoparts.application.command.UpdateRequestedPartCommand;
import com.autoparts.application.command.UpdateSupplierOfferCommand;
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
import java.util.Comparator;
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
    public List<RequestCase> getRequests(String sort, String status) {
        return requestCaseRepository.findAll().stream()
                .filter(requestCase -> matchesStatus(requestCase, status))
                .sorted(comparatorFor(sort))
                .toList();
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
    public RequestCase updateRequest(Long requestCaseId, UpdateRequestCommand command) {
        RequestCase requestCase = findRequestCase(requestCaseId);

        requestCase.getCustomer().setName(command.customerName());
        requestCase.getCustomer().setPhoneNumber(command.phoneNumber());
        requestCase.getCustomer().setEmail(command.email());
        requestCase.getCustomer().setType(command.customerType());

        requestCase.getVehicle().setBrand(command.vehicleBrand());
        requestCase.getVehicle().setModel(command.vehicleModel());
        requestCase.getVehicle().setProductionYear(command.vehicleProductionYear());
        requestCase.getVehicle().setVin(command.vin());
        requestCase.getVehicle().setLicensePlate(command.licensePlate());

        return requestCaseRepository.save(requestCase);
    }

    @Transactional(readOnly = true)
    public RequestedPart getRequestedPart(Long requestCaseId, Long requestedPartId) {
        return findRequestedPart(findRequestCase(requestCaseId), requestedPartId);
    }

    @Transactional
    public RequestCase updateRequestedPart(
            Long requestCaseId,
            Long requestedPartId,
            UpdateRequestedPartCommand command
    ) {
        RequestCase requestCase = findRequestCase(requestCaseId);
        RequestedPart requestedPart = findRequestedPart(requestCase, requestedPartId);

        requestedPart.setPartName(command.partName());
        requestedPart.setNote(command.note());

        return requestCaseRepository.save(requestCase);
    }

    @Transactional(readOnly = true)
    public SupplierOffer getSupplierOffer(Long requestCaseId, Long supplierOfferId) {
        return findSupplierOffer(findRequestCase(requestCaseId), supplierOfferId);
    }

    @Transactional
    public RequestCase updateSupplierOffer(
            Long requestCaseId,
            Long supplierOfferId,
            UpdateSupplierOfferCommand command
    ) {
        RequestCase requestCase = findRequestCase(requestCaseId);
        SupplierOffer supplierOffer = findSupplierOffer(requestCase, supplierOfferId);

        Money purchasePrice = new Money(
                command.purchasePriceAmount(),
                supplierOffer.getPurchasePrice().getCurrency()
        );
        PricingCalculationResult pricing = pricingService.calculate(
                purchasePrice,
                command.quantity(),
                requestCase.getCustomer().getType()
        );

        supplierOffer.setPartCode(command.partCode());
        supplierOffer.setPartName(command.partName());
        supplierOffer.setQuantity(command.quantity());
        supplierOffer.setPurchasePrice(purchasePrice);
        supplierOffer.setSellingPrice(pricing.sellingPricePerItem());
        supplierOffer.setSupplierName(command.supplierName());
        supplierOffer.setBrandName(command.brandName());

        return requestCaseRepository.save(requestCase);
    }

    @Transactional
    public void deleteRequest(Long requestCaseId) {
        RequestCase requestCase = findRequestCase(requestCaseId);
        requestCaseRepository.delete(requestCase);
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

    private RequestedPart findRequestedPart(RequestCase requestCase, Long requestedPartId) {
        return requestCase.getRequestedParts().stream()
                .filter(requestedPart -> requestedPartId.equals(requestedPart.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Requested part not found with id: " + requestedPartId));
    }

    private SupplierOffer findSupplierOffer(RequestCase requestCase, Long supplierOfferId) {
        return requestCase.getSupplierOffers().stream()
                .filter(supplierOffer -> supplierOfferId.equals(supplierOffer.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Supplier offer not found with id: " + supplierOfferId));
    }

    private boolean matchesStatus(RequestCase requestCase, String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return true;
        }

        return requestCase.getStatus() == RequestStatus.valueOf(status);
    }

    private Comparator<RequestCase> comparatorFor(String sort) {
        if ("vehicleNumber".equals(sort)) {
            return Comparator.comparing(this::vehicleNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        if ("customerName".equals(sort)) {
            return Comparator.comparing(
                    requestCase -> requestCase.getCustomer().getName(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
        }

        return Comparator.comparing(
                RequestCase::getCreatedDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
    }

    private String vehicleNumber(RequestCase requestCase) {
        String licensePlate = requestCase.getVehicle().getLicensePlate();
        if (licensePlate != null && !licensePlate.isBlank()) {
            return licensePlate;
        }

        return blankToNull(requestCase.getVehicle().getVin());
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }

    private void notifyStatusObservers(RequestCase requestCase, RequestStatus oldStatus, RequestStatus newStatus) {
        for (RequestStatusObserver observer : requestStatusObservers) {
            observer.onStatusChanged(requestCase, oldStatus, newStatus);
        }
    }
}
