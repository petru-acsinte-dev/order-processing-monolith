ALTER TABLE users.customer_user ADD "password" varchar(255) DEFAULT 'ADMIN' NOT NULL;
ALTER TABLE users.customer_user ALTER COLUMN "password" DROP DEFAULT;
