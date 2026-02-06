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
- created CustomerUserService for user CRUD admin operations
