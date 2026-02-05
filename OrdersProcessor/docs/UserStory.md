
User Story: Customer Order Management and Fulfillment Flow
===========================================================

Actors
------
*Customer*: A registered user who represents a single customer account and can place orders.  
*Admin*: A system administrator who can manager users, products, orders and fulfillment (internal usage).  
*Fulfillment service*: Responsible for preparing and packing orders.  
*Shipment service*: Responsible for shipping and delivering the orders.

Preconditions
-
The customer has an active registered user.

Active products exist in the system (with name, SKU, description and current price).

**1. Customer authentication**  
Customer logs in with their credentials.  
System:  
- validates credentials  
- loads customer information and past/current orders (only for the current user)

**2. Product selection**  
Customer browses the available products.  
System:  
- retrives all the active products with their info (name, SKU, description, current price)

Result  
The customer can select one or more products to add to an order.

**3. Order creation and management**  
Customer selects one or more products for purchase.  
System:
- creates a new order when the first products are selected
- maintains the order in a mutable state, allowing products to be added or removed
- snapshots each product name and current price into the order lines

Constraints
- the order can be modified until confirmed
- the order can be empty if all the added products are removed

Result  
The system maintains a draft order for the current user, reflecting the products, quatities and pricing.

**4. Order confirmation**  
The user confirms the order.  
System:
- locks up the order
- calculates the total amount for the order
- changes the order status to CONFIRMED

Result  
The confirmed order becomes immutable. It can only be cancelled.

**5. Order cancelled**  
The user confirms the cancellation for an order.  
System:
- order remains locked
- changes the order status to CANCELLED
- if the order status is already SHIPPED, the user receives an error stating that the order cannot be cancelled

Result  
The cancelled order becomes unavailable for fulfillment.

**6. Fulfillment**  
The fulfillment system retrieves the confirmed order.  
System:
- validates order lines and product availability
- prepares and packs the items for shipping
- updates the order status to READY_TO_SHIP

Result  
The order is ready for shipping.

**7. Shipping**  
The shipping service collects the packed order.  
System:
- marks the order as SHIPPED
- tracks the shipping details
