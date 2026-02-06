-- Create schema
CREATE SCHEMA IF NOT EXISTS ship;

-- Fulfillment and shipment status
CREATE TABLE ship.status (
	id SMALLINT PRIMARY KEY,
	status VARCHAR(50) NOT NULL
);

-- FulfillmentOrder table
CREATE TABLE ship.fulfillment_order (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    order_external_id UUID NOT NULL,
    status_id SMALLINT NOT NULL REFERENCES ship.status(id),
    created TIMESTAMPTZ DEFAULT NOW()
);

-- Shipment table
CREATE TABLE ship.shipment (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    order_external_id UUID NOT NULL,
    status_id SMALLINT NOT NULL REFERENCES ship.status(id),
    shipped TIMESTAMPTZ
);

-- Indexes
DROP INDEX IF EXISTS idx_fulfillment_order_external_id;
CREATE INDEX idx_fulfillment_order_external_id ON ship.fulfillment_order(external_id);
DROP INDEX IF EXISTS idx_shipment_order_external_id;
CREATE INDEX idx_shipment_order_external_id ON ship.shipment(order_external_id);

-- Comments
COMMENT ON COLUMN ship.fulfillment_order.external_id
	IS 'External unique fulfillment order identifier';

COMMENT ON COLUMN ship.fulfillment_order.order_external_id
	IS 'External unique order identifier';

COMMENT ON COLUMN ship.fulfillment_order.created
	IS 'Time when the fulfillment order was submitted';

COMMENT ON COLUMN ship.shipment.external_id
	IS 'External unique shipment order identifier';

COMMENT ON COLUMN ship.shipment.order_external_id
	IS 'External unique order identifier';

COMMENT ON COLUMN ship.shipment.shipped
	IS 'Time when the order was shipped';

-- Initialize data
INSERT INTO ship.status (id, status) VALUES
	(0, 'READY_TO_SHIP'),
	(1, 'CANCELLED'),
	(2, 'SHIPPED');
