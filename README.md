# SwissRoute 🚆

Backend REST API for planning and tracking public transport trips **within Switzerland**. Built with Java 21, Spring Boot, and PostgreSQL, SwissRoute acts as a business layer on top of the [Swiss Public Transport API](https://transport.opendata.ch/docs.html), allowing registered users to search connections, save favorite routes and stations, consult station boards, and keep a history of their planned trips.

> **Geographic scope:** SwissRoute is exclusively designed for the Swiss public transport network. All features — connection searches, station lookups, and timetables — rely on the Swiss Public Transport API (`transport.opendata.ch`), which only covers destinations and routes within Switzerland.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Database Migrations](#database-migrations)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Configuration](#environment-configuration)
  - [Running with Docker](#running-with-docker)
- [API Documentation](#api-documentation)
- [External API Reference](#external-api-reference)
- [Running Tests](#running-tests)
- [Contributing](#contributing)

---

## Tech Stack

| Layer            | Technology                  |
|------------------|-----------------------------|
| Language         | Java 21                     |
| Framework        | Spring Boot 3.5.x           |
| Database         | PostgreSQL                  |
| ORM              | Spring Data JPA / Hibernate |
| Migrations       | Flyway                      |
| HTTP Client      | WebClient (Spring WebFlux)  |
| Documentation    | Swagger / OpenAPI 3         |
| Security         | Spring Security + JWT       |
| Build Tool       | Maven                       |
| Containerization | Docker + Docker Compose     |

---

## Entity Relationship Diagram

The following diagram shows the data model that supports all SwissRoute features: user accounts, favorite routes, favorite stations, and connection search history.

![ER Diagram](doc/entity-relationship-model.png)

For a detailed description of each entity and its relationships, refer to the [full API documentation](doc/SwissRouteApi.md).

---

## Database Migrations

SwissRoute uses **Flyway** to manage the database schema. All migrations are versioned SQL scripts located under `src/main/resources/db/migration` and follow the naming convention `V{version}__{description}.sql`.

Flyway runs automatically on application startup — no manual steps are required. When the application starts for the first time, Flyway will:

1. Create the `flyway_schema_history` table if it does not exist.
2. Apply all pending migration scripts in version order.
3. Mark each migration as applied in the history table.

On subsequent startups, only new (unapplied) migrations are executed. Already-applied migrations are never re-run.

### Migration Script Location

```
src/
└── main/
    └── resources/
        └── db/
            └── migration/
                └── V1__initial_schema.sql
```

### Important Rules

> [!IMPORTANT]
> Never modify or delete an already-applied migration script. Flyway validates the checksum of each applied migration on startup — any change will cause the application to fail. To correct a past migration, always create a **new** versioned script.

> [!TIP]
> When adding a new database change (table, column, index, constraint), create a new migration script with the next version number. Include the corresponding Flyway script in the same PR as the feature that requires it.

---

## Getting Started

### Prerequisites

| Tool            | Minimum Version |
|-----------------|-----------------|
| Git             | 2.x             |
| Docker          | 24.x            |
| Docker Compose  | 2.x             |
| Java (JDK)      | 21              |
| Maven           | 3.9.x           |

---

### Environment Configuration

A `.env.template` file is provided at the repository root. Copy it and fill in the required values:

```bash
cp .env.template .env
```

Key variables include database credentials, the Swiss Public Transport API base URL (`https://transport.opendata.ch/v1`), and JWT secrets.

> [!IMPORTANT]
> Never commit your `.env` file. It is already listed in `.gitignore`. If it appears in `git status`, do not stage or commit it.

---

### Running with Docker

Spin up the application and its PostgreSQL database with a single command:

```bash
docker-compose up --build
```

Flyway migrations will run automatically on startup. The API will be available at `http://localhost:8080` once the container is healthy.

To run in detached mode:

```bash
docker-compose up --build -d
```

To stop all containers:

```bash
docker-compose down
```

To also remove volumes (wipes all database data, including applied migrations):

```bash
docker-compose down -v
```

> [!WARNING]
> Running `docker-compose down -v` drops all database volumes. On the next startup, Flyway will re-apply all migrations from scratch, resulting in an empty database.

---

## API Documentation

Once the application is running, the interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

The raw OpenAPI spec (JSON) is available at:

```
http://localhost:8080/v3/api-docs
```

The complete reference documentation for all endpoints, request/response schemas, and error codes is available at:

```
doc/SwissRouteApi.md
```

### Endpoint Summary

| Method   | Path                                                        | Auth | Description                                              |
|----------|-------------------------------------------------------------|------|----------------------------------------------------------|
| `POST`   | `/api/users/register`                                       | No   | Creates a new user account.                              |
| `POST`   | `/api/users/login`                                          | No   | Authenticates a user and returns a JWT token.            |
| `GET`    | `/api/stations`                                             | Yes  | Searches for stations by name or coordinates.            |
| `GET`    | `/api/connections`                                          | Yes  | Searches for connections between two stations.           |
| `GET`    | `/api/history`                                              | Yes  | Returns the authenticated user's connection search history. |
| `DELETE` | `/api/history/{id}`                                         | Yes  | Deletes a specific entry from the user's search history. |
| `DELETE` | `/api/history`                                              | Yes  | Clears all of the user's connection search history.      |
| `POST`   | `/api/favorite-routes`                                      | Yes  | Saves a new favorite route for the authenticated user.   |
| `GET`    | `/api/favorite-routes`                                      | Yes  | Returns all favorite routes for the authenticated user.  |
| `PUT`    | `/api/favorite-routes/{routeId}`                            | Yes  | Updates a favorite route by its ID.                      |
| `DELETE` | `/api/favorite-routes/{routeId}`                            | Yes  | Deletes a favorite route by its ID.                      |
| `POST`   | `/api/favorite-stations`                                    | Yes  | Saves a new favorite station for the authenticated user. |
| `GET`    | `/api/favorite-stations`                                    | Yes  | Returns all favorite stations for the authenticated user.|
| `DELETE` | `/api/favorite-stations/{externalStationId}`                | Yes  | Deletes a favorite station record.                       |
| `GET`    | `/api/favorite-stations/{externalStationId}/station-board`  | Yes  | Gets departures for a user's saved favorite station.     |
| `GET`    | `/api/station-board`                                        | Yes  | Gets station departures via query filters.               |

---

## External API Reference

SwissRoute integrates with the **Swiss Public Transport API** (`https://transport.opendata.ch/v1`). No API key is required.

> [!IMPORTANT]
> This API exclusively covers **public transportation within Switzerland**. Stations, routes, and connections outside Swiss territory are not available.

| Endpoint            | Used by                    |
|---------------------|----------------------------|
| `GET /locations`    | Station search             |
| `GET /connections`  | Connection search          |
| `GET /stationboard` | Station board (departures) |

Full documentation: [transport.opendata.ch/docs.html](https://transport.opendata.ch/docs.html)

---

## Running Tests

Run all tests with linters:

```bash
./mvnw -B clean verify
```

Run tests only (skipping linters):

```bash
./mvnw -B clean verify "-Dspotless.check.skip=true" "-Dcheckstyle.skip=true" "-Dpmd.skip=true"
```

Run integration tests against the external Transport API (requires internet connection):

```bash
./mvnw -B verify "-Dspotless.check.skip=true" "-Dcheckstyle.skip=true" "-Dpmd.skip=true" "-Dtransport.integration.tests=true"
```

Apply the code formatter before pushing:

```bash
./mvnw spotless:apply
```

---

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for branch conventions, commit format, linting requirements, and the PR process before opening your first pull request.
