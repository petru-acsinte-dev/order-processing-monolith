-- adding ISO 4217 currency column
ALTER TABLE orders.products ADD currency varchar(3) NULL;
COMMENT ON COLUMN orders.products.currency IS 'ISO 4217';

ALTER TABLE orders.orders ADD currency varchar(3) NULL;
COMMENT ON COLUMN orders.orders.currency IS 'ISO 4217';

ALTER TABLE orders.order_lines ADD currency varchar(3) NULL;
COMMENT ON COLUMN orders.order_lines.currency IS 'ISO 4217';

-- populating current column based on existing data
UPDATE orders.products p
SET currency = c.currency
FROM orders.currencies c
WHERE p.currency_id = c.id;

COMMIT;

UPDATE orders.orders o
SET currency = c.currency
FROM orders.currencies c
WHERE o.currency_id = c.id;

COMMIT;

UPDATE orders.order_lines ol
SET currency = c.currency
FROM orders.currencies c
WHERE ol.currency_id = c.id;

COMMIT;


-- drop currencies and FK
DROP TABLE orders.currencies CASCADE;

-- drop replaced column
ALTER TABLE orders.products DROP COLUMN currency_id;
ALTER TABLE orders.orders DROP COLUMN currency_id;
ALTER TABLE orders.order_lines DROP COLUMN currency_id;
