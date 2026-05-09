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
