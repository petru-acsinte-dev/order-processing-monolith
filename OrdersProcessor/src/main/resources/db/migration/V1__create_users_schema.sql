-- Create schema
CREATE SCHEMA IF NOT EXISTS users;

-- Roles table
CREATE TABLE IF NOT EXISTS users.roles (
	id SMALLINT PRIMARY KEY,
	role VARCHAR(50) NOT NULL
);

-- Status table
CREATE TABLE IF NOT EXISTS users.status (
	id SMALLINT PRIMARY KEY,
	status VARCHAR(50) NOT NULL
);

-- CustomerUser table
CREATE TABLE IF NOT EXISTS users.customer_user (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role_id SMALLINT NOT NULL,
    status_id SMALLINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    external_reference VARCHAR(255)
);

-- Indexes for faster lookup
CREATE INDEX IF NOT EXISTS idx_customer_user_name ON users.customer_user(username);
CREATE INDEX IF NOT EXISTS idx_customer_user_email ON users.customer_user(email);
CREATE INDEX IF NOT EXISTS idx_customer_user_external_id ON users.customer_user(external_id);

-- FKs
ALTER TABLE users.customer_user
ADD CONSTRAINT fk_role_id
FOREIGN KEY (role_id)
REFERENCES users.roles(id);

ALTER TABLE users.customer_user
ADD CONSTRAINT fk_status_id
FOREIGN KEY (status_id)
REFERENCES users.status(id);

