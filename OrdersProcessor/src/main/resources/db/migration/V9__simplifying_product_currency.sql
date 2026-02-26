-- adding ISO 4217 currency column
ALTER TABLE orders.products ADD currency varchar(3) NULL;
COMMENT ON COLUMN orders.products.currency IS 'ISO 4217';

-- populating currenct column based on existing data
UPDATE orders.products p
SET currency = c.currency
FROM orders.currencies c
WHERE p.currency_id = c.id;

-- drop currencies and FK
DROP TABLE orders.currencies CASCADE;

-- drop replaced column
ALTER TABLE orders.products DROP COLUMN currency_id;
