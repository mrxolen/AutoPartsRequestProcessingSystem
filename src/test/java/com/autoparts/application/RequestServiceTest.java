package com.autoparts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoparts.application.command.AddRequestedPartCommand;
import com.autoparts.application.command.AddSupplierOfferCommand;
import com.autoparts.application.command.CreateRequestCommand;
import com.autoparts.application.command.UpdateSupplierOfferCommand;
import com.autoparts.application.exception.RequestCaseNotFoundException;
import com.autoparts.application.factory.RequestCaseFactory;
import com.autoparts.application.observer.RequestStatusObserver;
import com.autoparts.application.observer.StatusHistoryObserver;
import com.autoparts.application.pricing.PricingStrategyResolver;
import com.autoparts.application.pricing.RegularPricingStrategy;
import com.autoparts.application.pricing.VipPricingStrategy;
import com.autoparts.application.pricing.WalkInPricingStrategy;
import com.autoparts.application.service.PricingService;
import com.autoparts.application.service.RequestService;
import com.autoparts.application.state.AcceptedState;
import com.autoparts.application.state.CompletedState;
import com.autoparts.application.state.NewState;
import com.autoparts.application.state.OfferReadyState;
import com.autoparts.application.state.RejectedState;
import com.autoparts.application.state.SearchingState;
import com.autoparts.application.state.SentToClientState;
import com.autoparts.application.state.StatusTransitionService;
import com.autoparts.domain.Customer;
import com.autoparts.domain.CustomerType;
import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.RequestedPart;
import com.autoparts.domain.SupplierOffer;
import com.autoparts.domain.Vehicle;
import com.autoparts.infrastructure.CustomerRepository;
import com.autoparts.infrastructure.RequestCaseRepository;
import com.autoparts.infrastructure.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestServiceTest {

    private RequestCaseRepository requestCaseRepository;
    private CustomerRepository customerRepository;
    private VehicleRepository vehicleRepository;
    private RequestStatusObserver notificationObserver;
    private RequestService requestService;

    @BeforeEach
    void setUp() {
        requestCaseRepository = mock(RequestCaseRepository.class);
        customerRepository = mock(CustomerRepository.class);
        vehicleRepository = mock(VehicleRepository.class);
        notificationObserver = mock(RequestStatusObserver.class);

        PricingService pricingService = new PricingService(new PricingStrategyResolver(List.of(
                new WalkInPricingStrategy(),
                new RegularPricingStrategy(),
                new VipPricingStrategy()
        )));

        StatusTransitionService statusTransitionService = new StatusTransitionService(List.of(
                new NewState(),
                new SearchingState(),
                new OfferReadyState(),
                new SentToClientState(),
                new AcceptedState(),
                new RejectedState(),
                new CompletedState()
        ));

        requestService = new RequestService(
                requestCaseRepository,
                customerRepository,
                vehicleRepository,
                new RequestCaseFactory(),
                pricingService,
                statusTransitionService,
                List.of(new StatusHistoryObserver(), notificationObserver)
        );

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestCaseRepository.save(any(RequestCase.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsRequestThroughFactoryAndSavesIt() {
        CreateRequestCommand command = new CreateRequestCommand(
                "polrig",
                null,
                null,
                CustomerType.REGULAR,
                "SEAT",
                "ALHAMBRA",
                2007,
                "VSSZZZ7MZ8V505695",
                "LV-1234"
        );

        RequestCase requestCase = requestService.createRequest(command);

        assertThat(requestCase.getCustomer().getType()).isEqualTo(CustomerType.REGULAR);
        assertThat(requestCase.getStatus()).isEqualTo(RequestStatus.NEW);
        assertThat(requestCase.getVehicle().getLicensePlate()).isEqualTo("LV-1234");
        verify(customerRepository).save(requestCase.getCustomer());
        verify(vehicleRepository).save(requestCase.getVehicle());
        verify(requestCaseRepository).save(requestCase);
    }

    @Test
    void addsRequestedPartAndSavesRequest() {
        RequestCase requestCase = requestCaseWithCustomerType(CustomerType.WALK_IN);
        when(requestCaseRepository.findById(1L)).thenReturn(Optional.of(requestCase));

        RequestCase savedRequest = requestService.addRequestedPart(
                1L,
                new AddRequestedPartCommand("front brake pads", "low dust preferred")
        );

        assertThat(savedRequest.getRequestedParts()).hasSize(1);
        RequestedPart requestedPart = savedRequest.getRequestedParts().getFirst();
        assertThat(requestedPart.getPartName()).isEqualTo("front brake pads");
        assertThat(requestedPart.getNote()).isEqualTo("low dust preferred");
        assertThat(requestedPart.getRequestCase()).isSameAs(requestCase);
        verify(requestCaseRepository).save(requestCase);
    }

    @Test
    void addsSupplierOfferWithCalculatedSellingPrice() {
        RequestCase requestCase = requestCaseWithCustomerType(CustomerType.REGULAR);
        when(requestCaseRepository.findById(1L)).thenReturn(Optional.of(requestCase));

        RequestCase savedRequest = requestService.addSupplierOffer(
                1L,
                new AddSupplierOfferCommand(
                        "BD-100",
                        "front brake discs",
                        2,
                        new BigDecimal("100.00"),
                        "EUR",
                        "Auto Supplier",
                        "Brembo"
                )
        );

        assertThat(savedRequest.getSupplierOffers()).hasSize(1);
        SupplierOffer supplierOffer = savedRequest.getSupplierOffers().getFirst();
        assertThat(supplierOffer.getPurchasePrice().getAmount()).isEqualByComparingTo("100.00");
        assertThat(supplierOffer.getSellingPrice().getAmount()).isEqualByComparingTo("125.00");
        assertThat(supplierOffer.getRequestCase()).isSameAs(requestCase);
        verify(requestCaseRepository).save(requestCase);
    }

    @Test
    void changesRequestStatusAndSavesRequest() {
        RequestCase requestCase = requestCaseWithCustomerType(CustomerType.VIP);
        requestCase.setStatus(RequestStatus.NEW);
        when(requestCaseRepository.findById(1L)).thenReturn(Optional.of(requestCase));

        RequestCase savedRequest = requestService.changeRequestStatus(1L, RequestStatus.SEARCHING);

        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.SEARCHING);
        assertThat(savedRequest.getStatusHistory()).hasSize(1);
        assertThat(savedRequest.getStatusHistory().getFirst().getOldStatus()).isEqualTo(RequestStatus.NEW);
        assertThat(savedRequest.getStatusHistory().getFirst().getNewStatus()).isEqualTo(RequestStatus.SEARCHING);
        assertThat(savedRequest.getStatusHistory().getFirst().getChangedDate()).isNotNull();
        verify(notificationObserver).onStatusChanged(requestCase, RequestStatus.NEW, RequestStatus.SEARCHING);
        verify(requestCaseRepository).save(requestCase);
    }

    @Test
    void filtersByStatusAndSortsByCustomerName() {
        RequestCase second = requestCaseForList("Zane", RequestStatus.SEARCHING, "LV-2000", "VIN-2000");
        RequestCase first = requestCaseForList("Anna", RequestStatus.SEARCHING, "LV-1000", "VIN-1000");
        RequestCase excluded = requestCaseForList("Bob", RequestStatus.NEW, "LV-3000", "VIN-3000");
        when(requestCaseRepository.findAll()).thenReturn(List.of(second, first, excluded));

        List<RequestCase> requests = requestService.getRequests("customerName", "SEARCHING");

        assertThat(requests).containsExactly(first, second);
    }

    @Test
    void sortsByVehicleNumberAndFallsBackToVin() {
        RequestCase vinOnly = requestCaseForList("Anna", RequestStatus.NEW, null, "AAA-001");
        RequestCase plate = requestCaseForList("Bob", RequestStatus.NEW, "BBB-002", "ZZZ-999");
        when(requestCaseRepository.findAll()).thenReturn(List.of(plate, vinOnly));

        List<RequestCase> requests = requestService.getRequests("vehicleNumber", "ALL");

        assertThat(requests).containsExactly(vinOnly, plate);
    }

    @Test
    void updatesSupplierOfferAndRecalculatesSellingPrice() {
        RequestCase requestCase = requestCaseWithCustomerType(CustomerType.REGULAR);
        SupplierOffer supplierOffer = new SupplierOffer();
        supplierOffer.setId(10L);
        supplierOffer.setPartCode("BD-100");
        supplierOffer.setPartName("front brake discs");
        supplierOffer.setQuantity(1);
        supplierOffer.setPurchasePrice(new com.autoparts.domain.Money(new BigDecimal("100.00"), "EUR"));
        supplierOffer.setSellingPrice(new com.autoparts.domain.Money(new BigDecimal("125.00"), "EUR"));
        supplierOffer.setSupplierName("Auto Supplier");
        supplierOffer.setBrandName("Brembo");
        supplierOffer.setRequestCase(requestCase);
        requestCase.getSupplierOffers().add(supplierOffer);
        when(requestCaseRepository.findById(1L)).thenReturn(Optional.of(requestCase));

        RequestCase savedRequest = requestService.updateSupplierOffer(
                1L,
                10L,
                new UpdateSupplierOfferCommand(
                        "BP-200",
                        "front brake pads",
                        2,
                        new BigDecimal("40.00"),
                        "Second Supplier",
                        "ATE"
                )
        );

        SupplierOffer updatedOffer = savedRequest.getSupplierOffers().getFirst();
        assertThat(updatedOffer.getPartCode()).isEqualTo("BP-200");
        assertThat(updatedOffer.getQuantity()).isEqualTo(2);
        assertThat(updatedOffer.getPurchasePrice().getAmount()).isEqualByComparingTo("40.00");
        assertThat(updatedOffer.getSellingPrice().getAmount()).isEqualByComparingTo("50.00");
        verify(requestCaseRepository).save(requestCase);
    }

    @Test
    void deletesExistingRequest() {
        RequestCase requestCase = requestCaseWithCustomerType(CustomerType.WALK_IN);
        when(requestCaseRepository.findById(1L)).thenReturn(Optional.of(requestCase));

        requestService.deleteRequest(1L);

        verify(requestCaseRepository).delete(requestCase);
    }

    @Test
    void throwsWhenRequestIsNotFound() {
        when(requestCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(99L))
                .isInstanceOf(RequestCaseNotFoundException.class)
                .hasMessage("Request case not found with id: 99");
    }

    private RequestCase requestCaseWithCustomerType(CustomerType customerType) {
        Customer customer = new Customer();
        customer.setType(customerType);

        RequestCase requestCase = new RequestCase();
        requestCase.setCustomer(customer);
        requestCase.setStatus(RequestStatus.NEW);

        return requestCase;
    }

    private RequestCase requestCaseForList(
            String customerName,
            RequestStatus status,
            String licensePlate,
            String vin
    ) {
        Customer customer = new Customer();
        customer.setName(customerName);

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setVin(vin);

        RequestCase requestCase = new RequestCase();
        requestCase.setCustomer(customer);
        requestCase.setVehicle(vehicle);
        requestCase.setStatus(status);
        requestCase.setCreatedDate(LocalDateTime.now());
        return requestCase;
    }
}
