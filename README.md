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

Services, UI, and business workflows will be added in later stages.

## Domain Model

The current domain model contains the core entities for processing an auto parts request:

- `Customer` with a `CustomerType`
- `Vehicle`
- `RequestCase` with a `RequestStatus`, created date, customer, vehicle, requested parts, and supplier offers
- `RequestedPart` for parts the customer needs
- `SupplierOffer` for supplier prices and part details
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

## Start PostgreSQL

Start the PostgreSQL container:

```bash
docker compose up -d
```

Database settings:

- Database: `autoparts_db`
- User: `autoparts_user`
- Password: `12345`
- Port: `5432`
