# EcoMarket Backend

API RESTful de **EcoMarket**, el buscador inteligente de comercio local y sostenible.
Proyecto del curso 1ASI0705 Arquitectura de Aplicaciones Web (UPC), Grupo 06.

Conecta a consumidores con comercios locales sostenibles cercanos. Los comerciantes publican sus productos y
Google Gemini les sugiere Eco-Etiquetas ("Venta a granel", "Sin envase plástico", "Producto local"…).

## Tecnologías

Java 21 · Spring Boot · Spring Data JPA · PostgreSQL (pgAdmin 4) · Spring Security · JWT · BCrypt ·
OpenAPI / Swagger · Postman · Google Gemini · OpenStreetMap Nominatim (geocodificación).

## Estructura

```text
src/main/java/pe/edu/upc/ecomarket
├── models        Entidades JPA (12 tablas + enum de estado del comercio)
├── dto           JSON de entrada y salida
├── repository    Interfaces JpaRepository
├── services      Lógica de negocio
├── controller    Endpoints REST
├── exceptions    Errores propios y manejador global (400, 401, 403, 404, 409)
├── security      JWT y filtro de autenticación
└── config        Spring Security, Swagger y datos iniciales
```

La guía paso a paso de cada fase (qué archivo, en qué carpeta y cómo probarlo) está en
[`docs/GUIA-BACKEND.md`](docs/GUIA-BACKEND.md).

## Cómo ejecutarlo

1. En **pgAdmin 4** crear la base de datos `ecomarket_db`.
2. Revisar usuario y contraseña de PostgreSQL en `src/main/resources/application.properties`
   (o definir `DB_USERNAME` y `DB_PASSWORD`).
3. Ejecutar en VS Code, o en la terminal:
   ```bash
   ./mvnw spring-boot:run
   ```
   (En Windows: `mvnw.cmd spring-boot:run`.) Las tablas y los datos iniciales se crean solos.
4. Abrir Swagger: http://localhost:8080/swagger-ui.html

Usuario administrador inicial: `admin@ecomarket.pe` / `Admin12345`.

## Pruebas

- **Automatizadas** (H2 en memoria, no necesitan PostgreSQL ni internet): `./mvnw test`. Son 25 pruebas de integración
  que cubren los criterios de aceptación de las User Stories y Technical Stories.
- **Postman:** importar [`postman/EcoMarket.postman_collection.json`](postman/EcoMarket.postman_collection.json) y ejecutarla
  completa con el *Collection Runner*. Son 78 peticiones, con los casos de error 400, 401, 403, 404 y 409. Los tokens y ids
  se guardan solos. Si la API corre en otro puerto, cambiar la variable `baseUrl`.

## Variables de entorno

| Variable | Uso | Por defecto |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/ecomarket_db`, `postgres`, `postgres` |
| `PORT` | Puerto HTTP | `8080` |
| `JWT_SECRETO` | Clave Base64 (mínimo 32 bytes) para firmar tokens | clave de desarrollo, **cambiar en producción** |
| `JWT_EXPIRACION_MINUTOS` | Duración del token | `120` |
| `ADMIN_CORREO`, `ADMIN_CONTRASENA` | Administrador inicial | `admin@ecomarket.pe`, `Admin12345` |
| `GEMINI_API_KEY` | Clasificación con Google Gemini | vacío: usa las palabras clave |
| `GEOCODIFICACION_HABILITADA` | Geocodificación con Nominatim | `true` |
| `CORS_ORIGENES` | Frontend permitido | `http://localhost:4200` |

> La API key de Gemini **nunca** se escribe en el código ni se sube a GitHub (`.env` está en `.gitignore`).

## Endpoints

| Recurso | Endpoints | Acceso | Historias |
|---|---|---|---|
| Autenticación | `POST /api/auth/register`, `POST /api/auth/login` | público | TS01, US01–US03 |
| Usuarios | `GET /api/usuarios`, `PATCH /api/usuarios/{id}/desactivar` | admin | US29 |
| Perfil | `GET`, `PUT /api/usuarios/perfil` | autenticado | US38 |
| Categorías | `GET /api/categorias[/{id}]` · `POST`, `PUT /{id}`, `DELETE /{id}` | público · admin | TS05 |
| Eco-Etiquetas | `GET /api/eco-etiquetas[/{id}]` · `POST`, `PUT /{id}`, `DELETE /{id}` (lógico si está en productos) | público · admin | TS05, US28 |
| Comercios | `POST /api/comercios`, `GET /mis-comercios` | comerciante | TS02, US19 |
| | `GET /api/comercios`, `GET /{id}`, `GET /{id}/productos` | público | US08, US10 |
| | `PUT /{id}`, `DELETE /{id}` | dueño o admin | US21 |
| | `GET /pendientes`, `PATCH /{id}/validacion` | admin | US30 |
| Búsqueda | `GET /api/busqueda/cercanos?lat&lng&radio`, `/productos?nombre&categoria&ecoEtiqueta`, `/comercios?texto` | público | TS03, TS04, US05–US07, US15 |
| Productos | `POST /api/productos` (clasifica con IA), `GET /mis-productos` | comerciante | TS04, US13, US20 |
| | `GET /{id}`, `GET /comparar?ids&lat&lng` | público | US11, US12 |
| | `PUT /{id}`, `DELETE /{id}`, `POST /{id}/clasificar` | dueño (o admin) | TS06, US16, US29 |
| Favoritos | `POST`, `GET /api/favoritos`, `DELETE /{id}` | consumidor | TS11, US09 |
| Preferencias | `GET`, `PUT /api/preferencias` | consumidor | TS11, US04 |
| Recomendaciones | `GET /api/recomendaciones/productos`, `/comercios?lat&lng` | consumidor | TS12, US17, US18 |
| Estadísticas | `GET /api/comercios/{id}/estadisticas?periodo` | dueño | TS13, US22, US23, US27 |
| Resumen | `GET /api/consumidor/resumen` | consumidor | US31 |
| Premium | `POST`, `GET /api/suscripciones` · `POST /api/promociones` | comerciante | TS14, US24–US26 |

## Flujo de trabajo (GitFlow)

`main` (versiones entregadas) · `develop` (integración) · `feature/fase-N-…` (una rama por fase, merge `--no-ff`)
· `release/<versión>`. Commits con Conventional Commits y scope, por ejemplo `feat(stores): add nearby search`.
