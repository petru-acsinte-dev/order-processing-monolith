-- Create schema
CREATE SCHEMA IF NOT EXISTS users;

-- User roles table
CREATE TABLE users.roles (
	id SMALLINT PRIMARY KEY,
	role VARCHAR(50) NOT NULL UNIQUE
);

-- User status table
CREATE TABLE users.status (
	id SMALLINT PRIMARY KEY,
	status VARCHAR(50) NOT NULL UNIQUE
);

-- CustomerUser table
CREATE TABLE users.customer_user (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role_id SMALLINT NOT NULL,
    status_id SMALLINT NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    address_line1 TEXT NOT NULL,
    address_line2 TEXT
);

-- Indexes for faster lookup
DROP INDEX IF EXISTS idx_customer_user_name;
CREATE INDEX idx_customer_user_name ON users.customer_user(username);
DROP INDEX IF EXISTS idx_customer_user_email;
CREATE INDEX idx_customer_user_email ON users.customer_user(email);
DROP INDEX IF EXISTS idx_customer_user_external_id;
CREATE INDEX idx_customer_user_external_id ON users.customer_user(external_id);

-- comments
COMMENT ON COLUMN users.customer_user.external_id 
	IS 'External, db independent, unique user identifier (UUID)';
COMMENT ON COLUMN users.customer_user.username 
	IS 'Customer username (unique)';
COMMENT ON COLUMN users.customer_user.email 
	IS 'Customer''s email (unique)';
COMMENT ON COLUMN users.customer_user.address_line1 
	IS 'Shipping address';
COMMENT ON COLUMN users.customer_user.address_line2 
	IS 'Shipping address, part 2, if necessary';

COMMENT ON COLUMN users.roles.role 
	IS 'Predefined user role; e.g. ADMIN, USER';

COMMENT ON COLUMN users.status.status 
	IS 'Predefined user status; e.g. ACTIVE, LOCKED, ARCHIVED';

-- initializing data
-- roles
INSERT INTO users.roles (id, role) VALUES
	(0, 'ADMIN'),
	(1, 'USER');

-- status
INSERT INTO users.status (id, status) VALUES
	(0, 'ACTIVE'), 
	(1, 'LOCKED'), 
	(2, 'ARCHIVED');

-- default admin
INSERT INTO users.customer_user
(	id, 
	external_id, 
	username, 
	email, 
	role_id, 
	status_id, 
	created, 
	address_line1)
VALUES
(	0, --reserved, outside sequence 
	'00000000-0000-0000-0000-000000000000', 
	'ADMIN', 
	'admin@order.processing.com', 
	0, -- admin role 
	0, -- active status
	now(), 
	'3401 Hillview Avenue, Palo Alto, CA 94304, USA'
);
