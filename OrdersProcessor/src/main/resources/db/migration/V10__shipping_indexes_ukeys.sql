ALTER TABLE ship.fulfillments ADD CONSTRAINT fulfillments_order_id_ukey UNIQUE (order_external_id);
ALTER TABLE ship.shipments ADD CONSTRAINT shipment_order_id_ukey UNIQUE (order_external_id);

DROP INDEX IF EXISTS idx_fulfillment_order_ext_id;
CREATE INDEX idx_fulfillment_order_ext_id ON ship.fulfillments(order_external_id);
DROP INDEX IF EXISTS idx_shipment_external_id;
CREATE INDEX idx_shipment_external_id ON ship.shipments(external_id);
