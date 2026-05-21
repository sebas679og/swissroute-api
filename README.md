# SwissRoute 🚆

Backend REST API for planning and tracking public transport trips in Switzerland. Built with Java + Spring Boot + 
PostgreSQL, it acts as a business layer on top of the [Swiss Public Transport API](https://transport.opendata.ch/docs.html), 
allowing registered users to search connections, save favorite routes and stations, consult station boards, and keep a 
history of their planned trips.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Database Model](#database-model)
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
| HTTP Client      | WebClient (Spring WebFlux)  |
| Documentation    | Swagger / OpenAPI 3         |
| Security         | Spring Security + JWT       |
| Build Tool       | Maven                       |
| Containerization | Docker + Docker Compose     |

---

## Database Model

```
usuarios
├── id             UUID PK
├── name         VARCHAR NOT NULL
├── email          VARCHAR UNIQUE NOT NULL
├── password       VARCHAR NOT NULL
├── base_city    VARCHAR
└── created_at     TIMESTAMP DEFAULT NOW()

rutas_favoritas
├── id             UUID PK
├── usuario_id     BIGINT FK → usuarios.id
├── nombre         VARCHAR NOT NULL
├── origen         VARCHAR NOT NULL
├── destino        VARCHAR NOT NULL
├── tipo_transporte VARCHAR
└── created_at     TIMESTAMP DEFAULT NOW()

historial_busquedas
├── id             UUID PK
├── usuario_id     BIGINT FK → usuarios.id
├── origen         VARCHAR NOT NULL
├── destino        VARCHAR NOT NULL
├── fecha_consulta TIMESTAMP NOT NULL
└── num_resultados INT

estaciones_favoritas
├── id                 UUID PK
├── usuario_id         BIGINT FK → usuarios.id
├── estacion_id_externo VARCHAR NOT NULL
├── nombre_estacion    VARCHAR NOT NULL
└── created_at         TIMESTAMP DEFAULT NOW()
```

---

## Getting Started

### Prerequisites

| Tool            | Minimum Version |
|-----------------|-----------------|
| Git             | 2.54.x          |
| Docker o Podman | 29.4.x          |
| Java (JDK)      | 21              |
| Maven           | 3.9.x           |

---

### Environment Configuration

A `.env.template` file is provided at the repository root. Copy it and fill in the required values:

```bash
cp .env.template .env
```

> [!IMPORTANT]
> Never commit your `.env` file. It is already listed in `.gitignore`.

---

### Running with Docker

Spin up the application and its PostgreSQL database with a single command:

```bash
docker-compose up --build
```

To run in detached mode:

```bash
docker-compose up --build -d
```

To stop all containers:

```bash
docker-compose down
```

To also remove volumes (wipes database data):

```bash
docker-compose down -v
```

The API will be available at `http://localhost:8080`.

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

### Endpoint Summary


| Method | Path                  | Description                                            |
|--------|-----------------------|--------------------------------------------------------|
| `POST` | `/api/users/register` | Creates a new user account in the SwissRoute platform. |
| `POST` | `/api/users/login`    | Authenticates a user and returns a JWT token.          |

---

## External API Reference

SwissRoute integrates with the **Swiss Public Transport API** (`https://transport.opendata.ch/v1`). No API key is required.

| Endpoint            | Used by           |
|---------------------|-------------------|
| `GET /locations`    | Station search    |
| `GET /connections`  | Connection search |
| `GET /stationboard` | Station board     |

Full documentation: [transport.opendata.ch/docs.html](https://transport.opendata.ch/docs.html)

> [!TIP]
> Start by mapping the external DTOs (`Connection`, `Stop`, `Journey`, `Section`, `Prognosis`) before building the service layer. The external API is well-documented and the object model is consistent.

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

Apply code formatter before pushing:

```bash
./mvnw spotless:apply
```

---

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for branch conventions, commit format, linting requirements, and the PR process before opening your first pull request.

## Documentation

The complete documentation of the API can be found at the following location: [doc/SwissRouteApi.md](doc/SwissRouteApi.md)
