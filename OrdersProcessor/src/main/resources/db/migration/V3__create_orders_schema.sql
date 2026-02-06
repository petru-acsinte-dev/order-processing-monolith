-- Create schema
CREATE SCHEMA IF NOT EXISTS orders;

-- Order status table
CREATE TABLE orders.status (
	id SMALLINT PRIMARY KEY,
	status VARCHAR(50) NOT NULL UNIQUE
);

-- Currencies table
CREATE TABLE orders.currencies (
	id SMALLINT PRIMARY KEY,
	currency CHAR(3) NOT NULL UNIQUE
);

-- Product table
CREATE TABLE orders.product (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    amount NUMERIC(19,4) NOT NULL, -- Money.amount
    currency_id SMALLINT NOT NULL REFERENCES orders.currencies(id) -- Money.currency
);

-- Order table
CREATE TABLE orders.orders (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    customer_external_id UUID NOT NULL,
    status_id SMALLINT NOT NULL REFERENCES orders.status(id),
    total_amount NUMERIC(19,4),
    currency_id SMALLINT NOT NULL REFERENCES orders.currencies(id),
    created TIMESTAMPTZ DEFAULT NOW()
);

-- OrderLine table
CREATE TABLE orders.order_line (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders.orders(id),
    product_id BIGINT NOT NULL REFERENCES orders.product(id),
    --product_sku VARCHAR(100), -- Can be added and populated later
    product_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    currency_id SMALLINT NOT NULL REFERENCES orders.currencies(id),
    quantity INT NOT NULL,
    line_total NUMERIC(19,4) NOT NULL
);

-- Indexes
DROP INDEX IF EXISTS idx_order_external_id;
CREATE INDEX idx_order_external_id ON orders.orders(external_id);
DROP INDEX IF EXISTS idx_orderline_order_id;
CREATE INDEX idx_orderline_order_id ON orders.order_line(order_id);

-- Comments
COMMENT ON COLUMN orders.currencies.currency
	IS 'Unique currency identifier; e.g. USD, EUR, CAD';

COMMENT ON COLUMN orders.product.sku
	IS 'External unique product identifier';

COMMENT ON COLUMN orders.orders.external_id
	IS 'External unique order identifier (UUID)';

COMMENT ON COLUMN orders.orders.customer_external_id
	IS 'External unique user identifier (UUID)';

COMMENT ON COLUMN orders.order_line.product_name
	IS 'Snapshot of product name at the time it was added to the order';

COMMENT ON COLUMN orders.order_line.unit_price
	IS 'Snapshot of product price at the time it was added to the order';

COMMENT ON COLUMN orders.order_line.quantity
	IS 'Product quantity added to the order';

COMMENT ON COLUMN orders.order_line.line_total
	IS 'Order line total (unit_price * quantity)';

-- initialize data
INSERT INTO orders.currencies (id, currency) VALUES
	(0, 'CAD'),
	(1, 'USD'),
	(2, 'EUR'),
	(3, '£');

-- status
INSERT INTO orders.status (id, status) VALUES
	(0, 'CREATED'), 
	(1, 'CANCELLED'), 
	(2, 'SHIPPED'),
	(3, 'CONFIRMED');

