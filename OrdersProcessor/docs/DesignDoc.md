
Order Processing System - Design Doc
====================================
Overview
--------
The project implementation concentrates on the proper development of backend services providing REST API endpoints for client interaction.
The frontend implementation is not considered a priority for this project. Tools like swagger or Postman and generators like 
Thymeleaf can be used later for visual interaction if required.  

Technologies
------------
- Spring Boot
- OpenAPI/swagger
- Hibernate persistence on PostgreSQL
- Git
- ~Jenkins~ GitHub CI/CD pipeline for continuous unit and integration testing (GitHub Actions was chosen for its simplicity, given the size of the project)

Goal
----
To have a stable and robust Spring Boot implementation on GitHub, split into multiple microservices each with its own database with migrated data,
with code-first OpenAPI generated specs, with the capability to run a CI/CD Actions pipeline.    

Trade-offs
----------
The system is initially designed as a monolith with a single database for persistence. However, subsequent phases plan to split the implementation
into several microservices and to split and migrate the database as well.  Therefore the initial monolithic design considers boundaries and 
microservices readiness.  
**NOTE**: This does not differ much from real work scenarios where projects are initially started using a monolithic approach (due to speed, costs etc.) and are split later, due to performance reasons, in preparation for estimated increased system loads and so on.

To save time and concentrate on the Spring ecosystem SDLC, some shortcuts will be initially made:
- a payment service is not considered at this time and its existence is implied; it could be considered much later, after the security layer
- users will be generated on the system (through a REST endpoint); the users security is not enabled initially, 
but the user's visibility (of orders) is a mandatory requirement
- active products (with name, SKU, description and current price) will be generated for the system 
(although that will be done through interactions with a REST endpoint)

**1. Approach**  
The system allows the customers (Customer) to browse products, create and modify orders, in order to receive shipments, while supporting 
administration, fulfillment, and future microservice scaling.  
Considerations given the planned future split:
- The entities are designed with long identifiers for intra-boundary database joins, lookups and admin operations.
- They entities also have external UUIDs for cross service communication.
- Snapshotting product information into order lines preserves the history (e.g. for audit purposes).
- To ensure consistent currency handling, embeddable Money objects are used.
- To ensure *decoupling* of services, in-memory events with listeners are initially introduced. This simplifies the future split into microservices without overcomplicating the monolithic design.

**2. Domain model**  
*2.1 CustomerUser* (combined User and Customer entity)  
Attributes: id (Long), externalId (UUID), username, email, role, status, externalReference (optional, e.g. CRM id)  
Trade-offs: Simplifies the user-customer mapping; future multi-customers-per-user scenarios would require redesign.

*2.2 Product*  
Attributes: id (Long), sku, name, description, active, currentPrice (Money) , external_id
Trade-offs: no historical prices if products are not included in orders and their price changes in the meantime.   
However order lines preserve history for already created orders.
Note: external_id (UUID) was added later for consistency (and in case products ever get split into a different microservice)

*2.3 Order and OrderLine*  
Order: id (Long), externalId (UUID), customerExternalId, status, totalAmount (Money), createdAt  
OrderLine: id (Long), orderId, productId, productName, unitPrice (Money), quantity, lineTotal (Money), createdAt  
**Note**: productId is used because products and orders live in the same boundary. If products ever need to be separated, a productSKU attribute 
can be added and populated before the migration. That productSKU will become the external identifier for products.
Temporary trade-offs: All prices in an order must use the same currency until a currency converter is implemented. Until then, an exception is thrown if a product is added to an order with a price currency different than the products already present in the order.
Workflow:
- Orders are created when the first product is selected
- OrderLines snapshot product information and pricing
- Confirmation locks the Order; cancellation allowed if not SHIPPED

**Note**: external ids (UUIDs) can be unwieldy for humans and error prone; for CRUD operations, it is easier to use internal IDs

*2.4 Fulfillment and shipping*  
FulfillmentOrder and Shipment track preparation and delivery using external ids for cross server references.  
Internal Ids are used for joining within boundaries.  
Trade-offs: Admins must use UUIDs to query fulfillment/shipping; but this ensures services independence.

**3. Workflow implementation**  
*a. Customer authentication*  
Simplified. Outside the scope of this project (for now).  
*x-USER* request header can be used to identify a user/customer through their unique email

*b. Product selection*  
Products retrieved via API; sku and currentPrice exposed

*c. Order creation and management*  
Order created on first selection; OrderLines preserve currentPrice

*d. Order confirmation*  
Locks Order and sets its status to CONFIRMED.

*e. Order cancelled*  
Sets the Order status to CANCELLED unless status is already SHIPPED.

*f. Fulfillment*  
Reads confirmed orders via external ID. Packs products and sets status to READY_TO_SHIP.

*g. Shipping*  
Delivers and updates status to SHIPPED, tracks shipment and updates status to DELIVERED.  
Needs to know the orders service that the order has been shipped, so cancellation is no longer possible.

**4. ID Strategy**  
- Internal long ids within boudaries (db PKs, internal joins, admin CRUD path variables, human readable operations)
- External Ids for cross-service communication, API exposure, and immutable references
- SKU Human friendly identifier for products

**5. Money handling**  
Money is an embeddable value object (BigDecimal amount, String currency).  
Used consistently across products, orders, order lines.  
Less error prone (e.g. currency mix) and avoids separate pricing tables.

**6. Trade-offs and decisions**  
a. User+Customer simplifies the model (1:1 relationship). A user cannot manage orders for multiple, different customers though.  
b. Order is mutable until locked; requires careful state management.  
c. Snapshotting product info into order lines duplicates data but preserves order history.  
d. Hard to read external IDs are used mainly for cross service communication.
*Note*: With the introduction of OpenAPI/Swagger UI, and especially with the presence of a proper UI, using the external ID is less error prone and even transparent to the end user. Internal ids would not be used in inter-service communication, but only in intra-service communication (in isolation) and only where convenience is a clear winner over error prone scenarios. The internal ids remain important for db joins (fast).

