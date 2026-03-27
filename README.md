# Order Processing: Monolith

The starting point of the [Order Processing](https://github.com/petru-acsinte-dev) 
portfolio project. This is a fully functional monolithic Spring Boot application, 
preserved as a reference for Phase 1 of the project's evolution toward a 
microservices architecture.

It demonstrates a production-style monolith with security, persistence, validation, 
testing, and a working CI/CD pipeline — before any architectural decomposition.

**REST API** — explore and test via Swagger UI at `http://localhost:8080/swagger-ui/index.html`

### Responsibilities
- User management and JWT authentication
- Product catalogue management
- Order lifecycle management (creation through confirmation)
- Fulfillment and shipment tracking
- Internal event-driven communication between domains

### Key Technologies
- Spring Boot 3.5 · Spring Security · PostgreSQL · Flyway · MapStruct
- Testcontainers · JaCoCo · OpenAPI/Swagger · Docker · GitHub Actions

### Project Documentation
- [User Story](https://github.com/petru-acsinte-dev/order-processing-monolith/blob/master/OrdersProcessor/docs/UserStory.md)
- [Design Document](https://github.com/petru-acsinte-dev/order-processing-monolith/blob/master/OrdersProcessor/docs/DesignDoc.md)
- [Project Journal](https://github.com/petru-acsinte-dev/order-processing-monolith/blob/master/OrdersProcessor/docs/journal/daily-journal.md)

### Evolution
This monolith has been decomposed into three independent microservices:
- [order-processing-users](https://github.com/petru-acsinte-dev/order-processing-users)
- [order-processing-orders](https://github.com/petru-acsinte-dev/order-processing-orders)
- [order-processing-shipments](https://github.com/petru-acsinte-dev/order-processing-shipments)

Shared infrastructure lives in:
- [order-processing-common](https://github.com/petru-acsinte-dev/order-processing-common)
