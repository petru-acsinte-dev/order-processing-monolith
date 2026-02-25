ALTER TABLE orders.products ADD external_id uuid NOT NULL;
ALTER TABLE orders.products ADD CONSTRAINT products_external_id_key UNIQUE (external_id);
ALTER TABLE orders.currencies ALTER COLUMN currency TYPE VARCHAR(3);
