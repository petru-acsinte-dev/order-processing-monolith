
-- FKs
ALTER TABLE users.customer_user
ADD CONSTRAINT fk_role_id
FOREIGN KEY (role_id)
REFERENCES users.roles(id);

ALTER TABLE users.customer_user
ADD CONSTRAINT fk_status_id
FOREIGN KEY (status_id)
REFERENCES users.status(id);
