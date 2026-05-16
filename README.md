# Auto Parts Request Processing System

## Project Idea

This project is a web-based Java application for processing customer requests for auto parts. The MVP will grow step by step and later include request status workflow, supplier offers, pricing calculation, and customer-ready report generation.

## Technology Stack

- Java 25
- Spring Boot
- Maven
- Thymeleaf
- HTML/CSS
- Lombok
- Spring Data JPA
- PostgreSQL
- Docker Compose
- Validation

## Architecture Plan

The project uses a simple layered package structure:

- `domain` - business entities and domain rules
- `application` - application services and use cases
- `infrastructure` - database and external integrations
- `web` - controllers and web layer

UI and additional business workflows will be added in later stages.

## Domain Model

The current domain model contains the core entities for processing an auto parts request:

- `Customer` with a `CustomerType`
- `Vehicle`
- `RequestCase` with a `RequestStatus`, created date, customer, vehicle, requested parts, and supplier offers
- `RequestedPart` for parts the customer needs
- `SupplierOffer` for supplier prices and part details
- `StatusHistoryEntry` for status change history
- `Money` as an embeddable value object for purchase and selling prices

For the MVP, these domain classes are also JPA entities so they can be stored in PostgreSQL.

## PostgreSQL Persistence

Spring Data JPA repositories are placed in the `infrastructure` package:

- `RequestCaseRepository` for saving and loading request cases
- `CustomerRepository` for customer records
- `VehicleRepository` for vehicle records

`RequestCaseRepository` extends `JpaRepository`, so it already supports common persistence operations such as `findAll`, `findById`, and `save`.

A simple development data loader creates one sample request when the database has no request cases yet. The sample request contains:

- Customer: `polrig`
- Vehicle: `SEAT ALHAMBRA 2007`
- VIN: `VSSZZZ7MZ8V505695`
- Requested parts: `front brake discs`, `front brake pads`, `rear springs`

## Pricing Calculation

Pricing calculation is implemented in the `application` package using the Strategy pattern.

Each customer type has its own pricing strategy:

- `WalkInPricingStrategy` adds a fixed markup of `15.00 EUR` per item
- `RegularPricingStrategy` adds `25%`
- `VipPricingStrategy` adds `15%`

`PricingStrategyResolver` chooses the correct strategy for the customer type, and `PricingService` uses it to calculate:

- selling price per item
- total purchase price
- total selling price
- profit

This keeps pricing rules separate and avoids putting all customer type logic into one large conditional block.

## Request Status Workflow

Request status changes are implemented in the `application` package using the State pattern.

Each request status has its own state class:

- `NewState`
- `SearchingState`
- `OfferReadyState`
- `SentToClientState`
- `AcceptedState`
- `RejectedState`
- `CompletedState`

Each state defines which statuses can come next. `StatusTransitionService` finds the current state for a `RequestCase` and asks it to apply the transition. Invalid transitions throw `InvalidStatusTransitionException`.

Valid transitions:

- `NEW` -> `SEARCHING`
- `SEARCHING` -> `OFFER_READY`
- `OFFER_READY` -> `SENT_TO_CLIENT`
- `SENT_TO_CLIENT` -> `ACCEPTED`
- `SENT_TO_CLIENT` -> `REJECTED`
- `ACCEPTED` -> `COMPLETED`

This keeps workflow rules close to each status and avoids one large conditional block.

## Application Service Layer

The `application` package contains `RequestService`, which coordinates request use cases:

- create a request
- get all requests
- get a request by id
- add a requested part
- add a supplier offer
- calculate supplier offer selling price through `PricingService`
- change request status through `StatusTransitionService`
- save changes through `RequestCaseRepository`

Small command DTOs are used as input objects:

- `CreateRequestCommand`
- `AddRequestedPartCommand`
- `AddSupplierOfferCommand`

## Request Factory

`RequestCaseFactory` uses the Factory pattern to create new `RequestCase` objects. It receives a `CreateRequestCommand`, checks the `CustomerType`, and creates the correct request setup for:

- `WALK_IN`
- `REGULAR`
- `VIP`

The factory keeps request construction in one place, while `RequestService` stays focused on coordinating persistence, pricing, and status changes.

## Status Change Observers

Request status changes use a simple Observer pattern.

`RequestService` changes the status through `StatusTransitionService`. After a successful transition, it notifies all `RequestStatusObserver` implementations:

- `StatusHistoryObserver` adds a `StatusHistoryEntry` to the request
- `ConsoleNotificationObserver` writes a simple log message

Each history entry stores:

- old status
- new status
- change date

This keeps status change side effects separate from the transition validation logic.

## Web UI

The first Thymeleaf UI is available through `RequestController`.

Current pages:

- request list: `/requests`
- create request: `/requests/new`
- request details: `/requests/{id}`
- add requested part: `/requests/{id}/parts/new`
- add supplier offer: `/requests/{id}/offers/new`

The request details page also includes:

- status change actions based on the current workflow state
- requested parts and supplier offers
- status history
- generated customer offer message

Controllers stay thin and delegate business work to `RequestService`. Form validation is added for required fields and numeric values. Shared labels are stored in `messages.properties`, and styling is kept in `static/css/styles.css`.

## Customer Offer Report

`ReportService` generates a customer-ready offer message from `RequestCase` data. It does not access repositories or the database directly.

The generated message includes:

- vehicle information and VIN
- supplier offer lines with part name, code, brand, quantity, and selling price
- total selling price

The request details page displays this message in a copyable text block.

## Start PostgreSQL

Start the PostgreSQL container:

```bash
docker compose up -d
```

Database settings:

- Database: `autoparts_db`
- User: `autoparts_user`
- Password: `12345`
- Port: `5433`

## PostgreSQL Troubleshooting

If the Docker volume was created with old credentials, recreate it:

```bash
docker compose down -v
docker compose up -d
```
