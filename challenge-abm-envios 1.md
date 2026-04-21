# Challenge Técnico: ABM de Envíos

## Contexto

Sos parte del equipo de desarrollo de una empresa de logística. Se necesita construir un microservicio que exponga una API REST para que **clientes externos** (por ejemplo, un e-commerce o un ERP) puedan dar de alta, consultar, modificar y dar de baja envíos.

El objetivo no es solo que funcione, sino que refleje las buenas prácticas que considerés apropiadas para un servicio productivo.

---

## Qué hay que construir

Un **ABM de Envíos** (Alta, Baja, Modificación) implementado como API REST en Spring Boot.

---

## Modelo de Dominio

### Envío (`Shipment`)

| Campo               | Tipo         | Reglas                                                             |
|---------------------|--------------|--------------------------------------------------------------------|
| `id`                | Long         | Autogenerado.                                                      |
| `codigoSeguimiento` | String       | Autogenerado al dar de alta (formato: `ENV-YYYYMMDD-XXXXX`). No modificable. |
| `nombreDestinatario`| String       | Obligatorio. Máximo 100 caracteres.                                |
| `direccionDestino`  | String       | Obligatorio. Máximo 255 caracteres.                                |
| `ciudadDestino`     | String       | Obligatorio.                                                       |
| `provinciaDestino`  | String       | Obligatorio.                                                       |
| `codigoPostal`      | String       | Obligatorio. Formato numérico de 4 dígitos.                       |
| `tipo`              | Enum         | `ESTANDAR`, `EXPRESO`, `FRAGIL`. Obligatorio.                     |
| `estado`            | Enum         | `PENDIENTE`, `EN_TRANSITO`, `ENTREGADO`, `CANCELADO`. Default: `PENDIENTE`. |
| `fechaCreacion`     | LocalDateTime| Autogenerado.                                                      |
| `fechaActualizacion`| LocalDateTime| Se actualiza en cada modificación.                                 |

---

## Endpoints Requeridos

### Alta

`POST /api/envios`

- Crea un nuevo envío con estado `PENDIENTE`.
- Genera automáticamente el `codigoSeguimiento`.
- Valida todos los campos obligatorios y sus restricciones.
- Retorna `201 Created` con el envío creado.

### Consulta

`GET /api/envios/{id}`

- Retorna el envío correspondiente.
- Si no existe, retorna `404 Not Found`.

`GET /api/envios`

- Lista envíos con soporte de **filtros opcionales**: `estado`, `tipo`, `ciudadDestino`.

`GET /api/envios/seguimiento/{codigoSeguimiento}`

- Permite consultar un envío por su código de seguimiento.

### Modificación

`PUT /api/envios/{id}`

- Permite modificar los datos del envío.
- **No se puede modificar** un envío en estado `ENTREGADO` o `CANCELADO`.
- **No se puede cambiar** el `codigoSeguimiento`, `fechaCreacion` ni el `id`.
- Retorna `200 OK` con el envío actualizado.

### Baja (lógica)

`DELETE /api/envios/{id}`

- No elimina físicamente el registro. Cambia el estado a `CANCELADO`.
- Solo se puede cancelar un envío en estado `PENDIENTE`.
- Si el envío está en `EN_TRANSITO`, `ENTREGADO` o ya está `CANCELADO`, retorna un error con un mensaje claro.
- Retorna `200 OK` con el envío actualizado.

### Cambio de Estado

`PATCH /api/envios/{id}/estado`

- Permite avanzar el estado del envío.
- Las transiciones válidas son:
  - `PENDIENTE` → `EN_TRANSITO`
  - `EN_TRANSITO` → `ENTREGADO`
  - `PENDIENTE` → `CANCELADO`
- Cualquier otra transición debe ser rechazada con un mensaje descriptivo.

---

## Reglas de Negocio

1. **Código de seguimiento**: Se genera automáticamente al crear el envío. Formato: `ENV-YYYYMMDD-XXXXX` donde `XXXXX` es un número secuencial de 5 dígitos con ceros a la izquierda (ejemplo: `ENV-20260415-00042`).

2. **Baja lógica**: Nunca se elimina un registro de la base de datos.

3. **Transiciones de estado**: Solo se permiten las transiciones definidas. El sistema debe validar que la transición sea válida y devolver un error claro si no lo es.

4. **Inmutabilidad parcial**: Los campos autogenerados (`id`, `codigoSeguimiento`, `fechaCreacion`) no pueden ser modificados por el cliente.

5. **Validaciones**: Todos los campos obligatorios deben validarse. Los errores de validación deben devolver `400 Bad Request` con detalle de qué campo falló y por qué.

---

**Nota:** No hay una única forma correcta de resolver esto. Queremos ver **tu criterio** al tomar decisiones de diseño.

---

## Stack Técnico

- **Java 17+**
- **Spring Boot 3.x**
- **DB aparte o en memoria, a libre decisión**
- **Maven** o **Gradle**

Podés agregar las librerías adicionales que consideres necesarias, siempre que las justifiques brevemente.

---

## Entrega

- Link a un repositorio público en Github
- Un archivo 'README.md' que explique:
 - Descripción breve de como levantar el proyecto
 - Alguna decisión de diseño que tomaste que quieras comentar
 - Qué mejorarías si tuvieras más tiempo

## Bonus (No obligatorio)

- Agregar la documentación que consideres apropiada para el proyecto.
- Agregar un endpoint `GET /api/envios/reporte` que devuelva un resumen con cantidad de envíos por estado.

---

¡Éxitos!
