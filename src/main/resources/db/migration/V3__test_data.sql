DELETE FROM shipments;

INSERT INTO shipments (
    id,
    tracking_code,
    recipient_name,
    destination_address,
    destination_city,
    destination_province,
    postal_code,
    type,
    status,
    active,
    created_at,
    updated_at
) VALUES
(gen_random_uuid(), 'ENV-20260420-00001', 'Juan Pérez', 'Av. Santa Fe 1234', 'CABA', 'Buenos Aires', '1425', 'ESTANDAR', 'DELIVERED', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00002', 'María García', 'Calle Falsa 123', 'La Plata', 'Buenos Aires', '1900', 'EXPRESO', 'IN_TRANSIT', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00003', 'Carlos López', 'Belgrano 456', 'Rosario', 'Santa Fe', '2000', 'ESTANDAR', 'PENDING', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00004', 'Ana Martínez', 'San Martín 789', 'Córdoba', 'Córdoba', '5000', 'FRAGIL', 'DELIVERED', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00005', 'Lucía Fernández', 'Corrientes 2020', 'CABA', 'Buenos Aires', '1001', 'ESTANDAR', 'CANCELLED', false, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00006', 'Diego Gómez', '9 de Julio 55', 'Mendoza', 'Mendoza', '5500', 'EXPRESO', 'IN_TRANSIT', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00007', 'Elena Rodríguez', 'Rivadavia 101', 'CABA', 'Buenos Aires', '1002', 'ESTANDAR', 'PENDING', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00008', 'Roberto Sánchez', 'Colón 333', 'Mar del Plata', 'Buenos Aires', '7600', 'EXPRESO', 'DELIVERED', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00009', 'Sonia Díaz', 'Alvear 999', 'CABA', 'Buenos Aires', '1100', 'FRAGIL', 'IN_TRANSIT', true, NOW(), NOW()),
(gen_random_uuid(), 'ENV-20260420-00010', 'Marcos Paz', 'Luro 4500', 'Mar del Plata', 'Buenos Aires', '7600', 'ESTANDAR', 'DELIVERED', true, NOW(), NOW());

SELECT setval('shipment_tracking_seq', 10);