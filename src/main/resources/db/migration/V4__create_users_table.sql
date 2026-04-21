CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Usuario de prueba: admin (password: admin)
INSERT INTO users (id, username, password, role)
VALUES (gen_random_uuid(), 'admin', '$2a$10$.h3tfCtnBO0iUJygqmJTG.G6uYzD5A/U6bZYG.stnn2scAl/51T1K', 'ADMIN');