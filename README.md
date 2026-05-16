# Auto Parts Request Processing System

## Project Idea

This project is a web-based Java application for processing customer requests for auto parts.

The application currently supports:

- creating customer auto parts requests
- adding vehicle information
- adding requested parts
- adding supplier offers
- customer-based pricing calculation
- request status workflow
- status history
- generated customer offer messages
- sorting and filtering requests
- editing and deleting requests
- grouped alternative supplier offers with price ranges
- a Thymeleaf web UI
- PostgreSQL persistence with Docker support

This is not a trivial CRUD application. In addition to storing data, it includes pricing calculation, workflow validation, observer side effects, grouped supplier offer reporting, and generated customer-ready reports.

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

The project uses a layered structure with clear responsibilities:

- `domain` - JPA entities, enums, and the core business model
- `application` - business services and design pattern implementations
- `application.pricing` - Strategy pattern classes for customer-based pricing
- `application.state` - State pattern classes for request workflow transitions
- `application.observer` - Observer pattern classes for status-change reactions
- `application.service` - orchestration services such as `RequestService`, `PricingService`, and `ReportService`
- `application.command` - command objects used as service inputs
- `infrastructure` - Spring Data JPA repositories and sample data loading
- `web` - controllers and web form classes
- `resources/templates` - Thymeleaf pages
- `resources/static` - CSS files

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

## Design Patterns

The project uses three main design patterns: Strategy, State, and Observer.

### Strategy Pattern

The Strategy pattern is used for customer-based pricing calculation.

Main classes and interfaces:

- `PricingStrategy`
- `WalkInPricingStrategy`
- `RegularPricingStrategy`
- `VipPricingStrategy`
- `PricingStrategyResolver`
- `PricingService`

Different customer types have different markup rules:

- `WALK_IN` uses a fixed markup
- `REGULAR` uses a percentage markup
- `VIP` uses a lower percentage markup

This is not artificial because pricing rules are real business logic in the application. New pricing rules can be added later without rewriting the existing pricing service.

### State Pattern

The State pattern is used for the request status workflow.

Main classes and interfaces:

- `RequestState`
- `NewState`
- `SearchingState`
- `OfferReadyState`
- `SentToClientState`
- `AcceptedState`
- `RejectedState`
- `CompletedState`
- `StatusTransitionService`

The application prevents invalid status transitions and models the real request workflow. For example, a request can move from `NEW` to `SEARCHING`, but it cannot jump directly from `NEW` to `COMPLETED`.

This is not artificial because a request cannot move freely between statuses; it must follow valid business steps.

### Observer Pattern

The Observer pattern is used after request status changes.

Main classes and interfaces:

- `RequestStatusObserver`
- `StatusHistoryObserver`
- `ConsoleNotificationObserver`
- `RequestService`

When a request status changes, observers can react automatically. In this project, observers record status history and log a simple notification.

This is not artificial because status history tracking is a real requirement for request processing.

These patterns satisfy the assignment requirement of using at least 2-3 design patterns with a clear purpose.

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

`RequestCaseFactory` is a small supporting helper that keeps new `RequestCase` construction in one place. It is not one of the three main required design patterns; it simply lets `RequestService` stay focused on coordinating persistence, pricing, and status changes.

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
- grouped supplier offer positions with part name and part code
- alternative brand options for the same part
- one total when all positions have one price, or a price range when alternatives exist

The request details page displays this message in a copyable text block.

## Request Management Features

The request list can now be sorted by vehicle number, customer name, or created date, and filtered by request status.

Requests can also be:

- edited after creation
- deleted from the request details page
- updated together with their requested parts and supplier offers

Supplier offers with the same part code and part name are treated as alternative manufacturer or brand options for one required part. These alternatives are not summed together, and the customer report shows a price range when more than one option is available.

## Docker Setup

For local IntelliJ development, start only PostgreSQL:

```bash
docker compose up -d postgres
```

The local Spring Boot configuration in `application.properties` connects to PostgreSQL on `localhost:5433`.

To run the full application with Docker Compose:

```bash
docker compose up -d
```

This starts both:

- the Spring Boot application at `http://localhost:8080`
- PostgreSQL for the application container

The Docker profile reads database connection settings from environment variables and connects to PostgreSQL through the Compose service name `postgres`.

Database settings:

- Database: `autoparts_db`
- User: `autoparts_user`
- Password: `12345`
- Port: `5433`

## Demo Flow

1. Start PostgreSQL with Docker:

   ```bash
   docker compose up -d postgres
   ```

2. Run the Spring Boot application.
3. Open `/requests`.
4. Create a request.
5. Add requested parts.
6. Add supplier offers.
7. Change the request status through the workflow.
8. View the generated customer offer message on the request details page.

## PostgreSQL Troubleshooting

If the Docker volume was created with old credentials, recreate it:

```bash
docker compose down -v
docker compose up -d
```
