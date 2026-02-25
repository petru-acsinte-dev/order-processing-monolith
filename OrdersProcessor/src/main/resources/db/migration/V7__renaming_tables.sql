ALTER TABLE orders.product RENAME TO products;
ALTER TABLE orders.order_line RENAME TO order_lines;
ALTER TABLE ship.fulfillment_order RENAME TO fulfillments;
ALTER TABLE ship.shipment RENAME TO shipments;
