OrderProcessor - Daily journal
=

2026-02-04
-
- user story / behavioral doc
- design doc

2026-02-05
-
- disabled Hibernate auto-DDL; started to use FlyWay scripts instead (avoid auto-DDL pitfalls) - had to force version 9.22.3 for Postgres 15 compatibility
- using local env variables for db credentials
- created users schema; separated status and role into very small tables to reduce redundancy and added FKs to custom_user
- created product, orders, order_line tables (orders schema) + status and currencies tables with FKs within boundaries and added indexes
- created fulfillment and shipment tables (ship schema) + status table with FKs within boundaries and added indexes
**Note1**: The order total amount cannot be calculated unless all the products have the same currency  
**Note2**: Some status table values are duplicated across orders and ship schemas. This provides service boundaries isolation for future services and database split. Using a single status table shared by orders and ship would complicated the migration to distributed services and databases.

2026-02-06
-
- created CustomerUser and associated entities + Address embeddable to be flattened inside CustomerUser
- created operation specific CustomerUser DTOs for creation, update and response to have explicit validation and accidental writes
- created CustomerUserRepository interface with added capabilities to identify the user by username/email (fake login), external id (update/delete)
- created CustomerUserService for user CRUD admin operations; added logging (avoided PII) and custom exceptions with generic messages (avoid PII)

2026-02-08
-
- created CustomerUserService unit tests with BDDMockito

2026-02-09
-
- created CustomerUserController with CRUD methods for /users endpoint and rudimentary user authentication/authorization

2026-02-11
-
- integration tests for /users endpoint
- switching to test containers vs. running tests against local Postgres DB maintained manually

2026-02-12
-
- replaced mappers with Mapstruct + mocked mappers in unit tests

2026-02-16
-
- setup GitHub remote repository for this project

2026-02-20
-
- introduced password in customer_user table (hashed with BCrypt)
- enhanced customer-user requests and entity to include password (creation and update)
- changed PUT to true PATCH request for user updates and refactored the user DTOs to reduce code duplication
- enabled spring-boot-security and added JWT dependencies

2026-02-23
-
- introduced JWTService, removed temporary authentication simulation methods from CustomerUserService
- created UserDetailsSecurityService used exclusively by JWT
- replaced UserHeaderFilter with JWTFilter
- created unit test for new UserDetailsSecurityService
- removed all references to the temporary x-user header, used to simulate authentication

2026-02-24
-
- had troubles with JWTFilter; for unexplained reasons, the POST request body would be consumed before it got to AuthController
- introduced JsonLoginFilter to precede JWTFilter and obtain the request body and convert to credentials
- allowed public access to swagger (docs) and actuator (monitoring) endpoints for now
- pushed latest to GitHub -> clean build + deployable Docker image
- enhanced OpenAPI for auth controller
- fixed 200 response on bad login with a custom AuthenticationEntryPoint
- fixed swagger content for error response (no content for 401/403)

2026-02-25
-
- renamed db tables for consistency (noticed that FlyWay and DevTools is not a good combination; if sql is partially saved, DevTools restarts the app migrating before the script is finished, resulting in errors on subsequent automatic restarts)
- updated the design doc based on the in-flight decisions to use GitHub Actions (instead of Jenkins) and in-memory events for services communication
- fixed user creation without encrypted password: [Issue #2](https://github.com/petru-acsinte/order-processing/issues/2)
- started work on products endpoint
- created sample data for users and products; adjusted test expectations

2026-02-26
-
- tidying up validation annotations, reorganizing packages
- simplifying currency for products; removing the currency entity (reason: it's supposed to be predefined and immutable for this project; no rates changes either or other aspects that would make currency mutable)
- using Money embeddable for Product entity
- defined product DTOs + mappers and mappers test
- make product SKU immutable; once created it cannot be changed through a request
- regenerated OrderProcessor-ER diagram to reflect the latest relational changes
- product service CRUD operations + creation unit test

2026-03-03
-
- product service update + delete unit tests
- created product controller

2026-03-04
-
- product integration CRUD tests
- moving common members/methods to IT base class
- moving class in shared package
- improved test setup performance by replacing heavy db construction before/after with a single FlyWay migration and using @Rollback in tests
- various fixes for missed Java Doc, annotations etc.

2026-03-06
-
- preventing the admin user from being deleted
- removing temporary requestor parameter from customer user service (using JWT security instead)

2026-03-07
-
- introducing paging for listing users
- ran into problems with @ConfigurationProperties not initializing properly; turns out Eclipse STS warnings around properties in the properties file are red herrings

2026-03-09
-
- fixed link templates and sorting url params
- introducing paging for products

2026-03-10
-
- refactored products package to orders (to include products and orders)

2026-03-11
-
- orders DTOs and mappers

2026-03-12
-
- orders mapper tests
- orders creation: service + controller + integration test
- fixes for incorrect http status on product responses

2026-03-13
-
- order update: service + controller + integration test
- added integration test for removing products from an order
- getting an order + listing accessible orders (for current owner or by specified owner) in controller + service
- documented order controller and service

2026-03-17
-
- order service status update; moved OrderStatus to an enum and renamed it status to avoid conflicts with OrderStatus entity
- integration tests for getting orders and updating order status based on its current status
