# Shipment Service

Microservicio REST para gestión del ciclo de vida de envíos (ABM). Construido con Spring Boot 3, Clean Architecture y DDD. Autenticación stateless con JWT.

---

## Cómo levantar el proyecto

### Requisitos previos

- Docker Desktop instalado y en ejecución
- Java 21
- Gradle (o usar el wrapper `./gradlew`)

### 1. Levantar la base de datos con Docker Compose

```bash
docker compose up -d
```

Esto levanta PostgreSQL en `localhost:5432` con la base de datos `shipment_db`.  
Flyway aplica automáticamente las migraciones (esquema + datos de prueba) al iniciar la aplicación.

### 2. Ejecutar la aplicación

```bash
./gradlew bootRun
```

La aplicación queda disponible en `http://localhost:8080`.

### 3. Probar desde Swagger UI

Abrir en el navegador:

```
http://localhost:8080/swagger-ui/index.html
```

**Credenciales por defecto** (usuario administrador creado por migración):

| Campo    | Valor   |
| -------- | ------- |
| username | `admin` |
| password | `admin` |

**Flujo de autenticación en Swagger:**

1. Ejecutar `POST /api/v1/auth/login` con `{ "username": "admin", "password": "admin" }`.
2. Copiar el `token` de la respuesta.
3. Hacer clic en **Authorize** (ícono del candado) e ingresar `Bearer <token>`.
4. Usar los demás endpoints normalmente.

---

## Endpoints disponibles

| Método   | Ruta                                     | Descripción                             | Rol mínimo  |
| -------- | ---------------------------------------- | --------------------------------------- | ----------- |
| `POST`   | `/api/v1/auth/login`                     | Obtener token JWT                       | Público     |
| `POST`   | `/api/v1/shipments`                      | Crear envío                             | `ADMIN`     |
| `GET`    | `/api/v1/shipments`                      | Listar envíos (filtros + paginación)    | `USER`      |
| `GET`    | `/api/v1/shipments/{id}`                 | Obtener envío por ID                    | `USER`      |
| `GET`    | `/api/v1/shipments/seguimiento/{codigo}` | Obtener envío por código de seguimiento | `USER`      |
| `PUT`    | `/api/v1/shipments/{id}`                 | Actualizar datos del envío              | Autenticado |
| `DELETE` | `/api/v1/shipments/{id}`                 | Baja lógica (PENDING → CANCELLED)       | Autenticado |
| `PATCH`  | `/api/v1/shipments/{id}/estado`          | Cambiar estado del envío                | Autenticado |
| `GET`    | `/api/v1/shipments/reporte`              | Descargar reporte PDF por estado        | `USER`      |

### Transiciones de estado válidas

```
PENDING ──► IN_TRANSIT ──► DELIVERED
   │
   └──────────────────────► CANCELLED
```

---

## Ejecutar los tests

```bash
# Sólo tests unitarios y de integración de capa web (sin DB)
./gradlew test

# Con reporte HTML en build/reports/tests/test/index.html
./gradlew test --info
```

> **Nota:** el test `ShipmentServiceApplicationTests` (context load) requiere que Docker Compose esté corriendo.

### Estructura de tests

```
src/test/
├── unit/
│   ├── domain/
│   │   └── ShipmentTest                    ← reglas de negocio del modelo
│   └── service/
│       ├── CreateShipmentServiceTest       ← creación con reintentos
│       ├── CancelShipmentServiceTest       ← baja lógica
│       ├── UpdateShipmentServiceTest       ← modificación con validación de estado
│       ├── UpdateShipmentStatusServiceTest ← máquina de estados
│       ├── GetShipmentByIdServiceTest
│       ├── GetShipmentByTrackingServiceTest
│       └── TrackingCodeGeneratorImplTest   ← formato ENV-YYYYMMDD-NNNNN
└── integration/
    ├── ShipmentControllerIT    ← HTTP + seguridad + validación (@WebMvcTest)
    └── AuthenticationControllerIT
```

---

## Decisiones de diseño

### Arquitectura en capas (Clean Architecture)

- **`domain/`**: modelo rico (`Shipment` con métodos de negocio), interfaces de repositorio y servicios. Sin dependencias de Spring.
- **`application/`**: casos de uso (un interface + un servicio por operación). Orquesta el dominio.
- **`infrastructure/`**: JPA, seguridad JWT, exportación PDF, migraciones Flyway.
- **`controller/`**: delega exclusivamente en los casos de uso.

### Modelo de dominio rico

`Shipment` encapsula todas las reglas de negocio en métodos propios (`cancel()`, `markAsInTransit()`, etc.), lanzando `InvalidShipmentStateException` ante transiciones inválidas. Esto garantiza consistencia independientemente de qué capa llame al modelo.

### Código de seguimiento generado por secuencia DB

Se usa una secuencia PostgreSQL (`shipment_tracking_seq`) en lugar de un contador en memoria. Esto garantiza unicidad incluso con múltiples instancias del servicio.

### Baja lógica

Los envíos nunca se eliminan físicamente. Se marca `status = CANCELLED` y `active = false`. La columna `active` permite filtrar sin mezclar lógica de negocio con lógica de presentación.

### Seguridad JWT stateless

- Tokens firmados con HS256 y expiración de 24 horas.
- Sin sesión del lado del servidor (`SessionCreationPolicy.STATELESS`).
- Roles: `ADMIN` (escritura), `USER` (lectura). El endpoint de login es público.

### Validaciones en dos capas

1. **HTTP** (Bean Validation): campos obligatorios, tamaños máximos, formato del código postal.
2. **Dominio**: transiciones de estado, inmutabilidad de `id`, `trackingCode`, `fechaCreacion`.

---

## Qué mejoraría en producción

| Ítem                                   | Descripción                                                                                                                                                             |
| -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Secret JWT externalizado**           | El `SECRET_KEY` en `JwtService` debería venir de una variable de entorno o un vault (ej. AWS Secrets Manager).                                                          |
| **Integración real con Kafka**         | La dependencia está declarada pero el producer está vacío. Publicar eventos de dominio (`ShipmentCreated`, `ShipmentStatusChanged`) permitiría desacoplar consumidores. |
| **Tests de integración completos**     | Agregar Testcontainers para correr tests con PostgreSQL real en CI/CD.                                                                                                  |
| **Refresh token**                      | Implementar refresh tokens para no requerir nuevo login cada 24 h.                                                                                                      |
| **Roles más granulares**               | El esquema actual tiene `USER` y `ADMIN`. Podría extenderse a `OPERATOR` para cambios de estado.                                                                        |
| **Observabilidad**                     | Configurar Actuator con métricas en Micrometer + Prometheus para monitoreo.                                                                                             |
| **Manejo de `countShipmentsByStatus`** | Actualmente carga todas las entidades en memoria. Debería usar una query de proyección con `GROUP BY`.                                                                  |
| **Paginación del reporte**             | El reporte PDF no tiene límite de registros. Con alto volumen puede agotar la memoria.                                                                                  |
