CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    tracking_code VARCHAR(255) UNIQUE NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    destination_address VARCHAR(255) NOT NULL,
    destination_city VARCHAR(255) NOT NULL,
    destination_province VARCHAR(255) NOT NULL,
    postal_code VARCHAR(4) NOT NULL,
    type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);