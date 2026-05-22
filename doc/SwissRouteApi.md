# SwissRoute — Backend for Public Transport Trip Planning and Tracking

---

## Table of Contents

- [Problem Statement](#problem-statement)
- [Proposed Solution](#proposed-solution)
- [Project Justification](#project-justification)
- [Technology Stack](#technology-stack)
- [External API Integration](#external-api-integration)
- [Authentication Service](#authentication-service)
  - [Register User](#register-user)
  - [Login User](#login-user)
- [Stations Service](#stations-service)
  - [Search Stations By Name](#search-stations-by-name)

---

## Problem Statement

Modern public transportation systems generate a large amount of route, schedule, and connection data. However, accessing and organizing this information efficiently remains a challenge for many users. Travelers often need to consult multiple platforms to plan routes, verify schedules, and manage their preferred trips, resulting in a fragmented and inconvenient experience.

Additionally, most public transport APIs provide raw transportation data but lack personalized features such as favorite route management, travel history tracking, and centralized trip planning. This creates an opportunity to build a backend system capable of transforming public transportation data into a structured, scalable, and user-oriented service.

SwissRoute addresses this problem by providing a robust backend API that integrates with the **Swiss Public Transport API** (`https://transport.opendata.ch`) and offers advanced functionalities for authenticated users, including connection searches, favorite route storage, timetable consultation, and travel history management.

> **Important geographic constraint:** The Swiss Public Transport API exclusively covers the Swiss public transport network — trains, buses, trams, boats, and cable cars operating within Switzerland. SwissRoute therefore only supports travel planning for Swiss destinations. International routes or stations outside Switzerland are not supported by the underlying data source.

---

## Proposed Solution

SwissRoute is a RESTful backend application developed using Java 21, Spring Boot, and PostgreSQL. The system acts as a business layer on top of the Swiss Public Transport API, enriching the external transport data with user management and personalized travel features.

The application provides:

* User authentication and account management.
* Public transport connection searches between stations.
* Timetable and schedule consultation.
* Favorite route persistence for quick access.
* Historical tracking of planned trips.
* Clean and scalable REST API architecture.
* Interactive API documentation using Swagger / OpenAPI.

By combining external transportation services with internal business logic and persistent storage, SwissRoute delivers a reliable backend platform suitable for future scalability and integration with web or mobile frontends.

---

## Project Justification

The development of SwissRoute is justified by the increasing demand for digital mobility solutions capable of improving the user experience in public transportation systems. As urban mobility becomes more dependent on technology, backend services must provide not only access to transportation data but also personalization, reliability, and scalability.

This project demonstrates the implementation of modern backend development practices using enterprise-level technologies such as Spring Boot, Spring Data JPA, PostgreSQL, and reactive HTTP communication with WebClient. Furthermore, it promotes software engineering principles including layered architecture, API-first design, database persistence, and secure user management.

From an educational and professional perspective, SwissRoute serves as a practical example of how to integrate third-party APIs into a scalable backend ecosystem while maintaining clean code standards, maintainability, and extensibility.

The project also provides a strong foundation for future enhancements such as real-time notifications, geolocation support, route optimization algorithms, and frontend integration for complete mobility platforms.

---

## Technology Stack

| Layer                | Technology                  |
|----------------------|-----------------------------|
| Programming Language | Java 21+                    |
| Framework            | Spring Boot 3.5.x           |
| Database             | PostgreSQL                  |
| ORM                  | Spring Data JPA / Hibernate |
| HTTP Client          | WebClient                   |
| Documentation        | Swagger / OpenAPI           |
| Build Tool           | Maven                       |

---

## External API Integration

SwissRoute is built on top of the **Swiss Public Transport API**, an open and free data source provided by the Swiss transport community.

| Property       | Value                                      |
|----------------|--------------------------------------------|
| Base URL       | `https://transport.opendata.ch/v1`         |
| Authentication | None required (public API)                 |
| Documentation  | https://transport.opendata.ch/docs.html    |
| Coverage       | Switzerland only                           |

### Endpoints Used

| Endpoint            | Purpose in SwissRoute             |
|---------------------|-----------------------------------|
| `GET /locations`    | Search stations by name           |
| `GET /connections`  | Find connections between stations |
| `GET /stationboard` | Get departures from a station     |

### Geographic Scope

> **This API only covers public transport within Switzerland.**

The Swiss Public Transport API provides data for the Swiss national transport network, including:

- 🚆 Trains (SBB/CFF/FFS, regional railways)
- 🚌 Buses (intercity and local lines)
- 🚋 Trams and urban transit
- ⛵ Lake boats
- 🚠 Cable cars and funiculars

**Stations, routes, and connections outside Switzerland are not available.** Queries for international destinations (e.g., Paris, Milan, Munich) will return no results or may partially match border stations. SwissRoute does not attempt to bridge this limitation — it is an intentional constraint of the underlying data source.

---

## Authentication Service

### Register User

Creates a new user account in the SwissRoute platform.

#### Endpoint

```http
POST /api/users/register
```

#### Access

Public

---

#### Request Body

```json
{
  "name": "Joe Doe",
  "email": "joedoe@email.com",
  "password": "Password123!",
  "baseCity": "Madrid"
}
```

---

#### Request Fields

| Field      | Type   | Required | Description                               |
|------------|--------|----------|-------------------------------------------|
| `name`     | String | Yes      | Full name of the user                     |
| `email`    | String | Yes      | Unique user email address                 |
| `password` | String | Yes      | User password following security rules    |
| `baseCity` | String | Yes      | User's default city for travel operations |

---

#### Password Security Rules

The password must satisfy the following requirements:

* Minimum length of 8 characters
* At least one uppercase letter
* At least one lowercase letter
* At least one numeric digit
* At least one special character

#### Valid Password Example

```text
Password123!
```

---

#### Password Encryption

For security purposes, user passwords are never stored in plain text.

Before persisting the user into the database, the password is encrypted using:

```text
BCrypt Password Encoder
```

This ensures secure password hashing and protection against credential exposure.

---

#### Successful Response

##### 201 — Created

```json
{
  "name": "Joe Doe",
  "email": "joedoe@email.com",
  "baseCity": "Madrid",
  "createdAt": "2026-05-20T00:04:05.836Z"
}
```

---

#### Error Responses

##### 409 — Conflict

Occurs when the provided email is already registered in the system.

```json
{
  "code": 409,
  "name": "CONFLICT",
  "description": "Email is already in use.",
  "timestamp": "2026-05-20T00:04:38.297Z"
}
```

---

##### 400 — Bad Request

Occurs when one or more required fields are missing or invalid.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "email: Email is required; name: Name is required; password: Password is required; baseCity: Base city is required",
  "timestamp": "2026-05-20T00:05:24.008Z"
}
```

---

#### Validation Rules

| Field      | Validation                                         |
|------------|----------------------------------------------------|
| `name`     | Must not be empty                                  |
| `email`    | Must not be empty and must be a valid email format |
| `password` | Must comply with all password security rules       |
| `baseCity` | Must not be empty                                  |

---

#### Notes

* The email address must be unique across the platform.
* Passwords are encrypted using BCrypt before database persistence.
* Validation errors are aggregated into a single response message for better client-side handling.
* Timestamps are returned in ISO-8601 UTC format.

---

### Login User

Authenticates an existing user and generates a JWT access token for authorized requests.

##### Endpoint

```http
POST /api/users/login
```

#### Access

Public

---

#### Request Body

```json
{
  "email": "joedoe@email.com",
  "password": "Password123!"
}
```

---

##### Request Fields

| Field      | Type   | Required | Description                    |
|------------|--------|----------|--------------------------------|
| `email`    | String | Yes      | Registered user email address  |
| `password` | String | Yes      | User account password          |

---

#### Authentication Process

During authentication:

1. The system validates the request payload.
2. The user is searched by email address.
3. The provided password is compared against the encrypted password stored in the database using BCrypt.
4. If authentication succeeds, a JWT token is generated and returned to the client.

---

##### Successful Response

##### 200 — OK

```json id="x2c7nr"
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkNTIxOGNiNC1iYjU3LTRlYTktYjAwMi1mMmU3MzUwNWQwNDEiLCJlbWFpbCI6ImpvZWRvZUBlbWFpbC5jb20iLCJpYXQiOjE3NzkzMjI2OTQsImV4cCI6MTc3OTMyNjI5NH0.GOV38Fwa9xDDNjqsFILinGixmLg2qLYi-_8J_ymTFrc",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "d5218cb4-bb57-4ea9-b002-f2e73505d041"
}
```

---

###### Response Fields

| Field       | Type   | Description                                 |
|-------------|--------|---------------------------------------------|
| `token`     | String | JWT access token                            |
| `tokenType` | String | Authentication scheme used                  |
| `expiresIn` | Number | Token expiration time in seconds            |
| `userId`    | UUID   | Unique identifier of the authenticated user |

---

###### JWT Security

The generated JWT token contains authenticated user information and is digitally signed to guarantee integrity and authenticity.

The token is required for accessing protected endpoints and must be sent using the following header format:

```http
Authorization: Bearer <token>
```

---

##### Error Responses

##### 400 — Bad Request

Occurs when required fields are missing, empty, or null.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "password: Password is required; email: Email is required",
  "timestamp": "2026-05-21T00:18:38.544Z"
}
```

---

##### 401 — Unauthorized

Occurs when the provided credentials are invalid.

```json id="b7yt3f"
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Invalid credentials",
  "timestamp": "2026-05-21T00:19:05.308Z"
}
```

---

#### Validation Rules

| Field      | Validation                                                |
|------------|-----------------------------------------------------------|
| `email`    | Must not be null, empty, and must be a valid email format |
| `password` | Must not be null or empty                                 |

---

##### Notes

* All request fields are mandatory.
* Empty strings and null values are not accepted.
* Password verification is performed using BCrypt password matching.
* JWT tokens are time-limited and expire automatically after the configured duration.
* Timestamps are returned in ISO-8601 UTC format.

---

## Stations Service

### Search Stations By Name

Returns a list of public transport stations matching the provided search query.

#### Endpoint

```http
GET /api/stations?query={station}
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

The endpoint requires a valid JWT token in the request header.

```http
Authorization: Bearer {{JWT}}
```

---

#### Query Parameters

| Parameter | Type   | Required | Description                                    |
|-----------|--------|----------|------------------------------------------------|
| `query`   | String | Yes      | Station name or partial station name to search |

---

#### Example Request

```http
GET /api/stations?query=Basel
```

---

#### Successful Response

##### 200 — OK

```json
{
  "stations": [
    {
      "id": "8500010",
      "name": "Basel SBB",
      "latitude": 47.547403,
      "longitude": 7.589564
    },
    {
      "id": "8500090",
      "name": "Basel Bad Bf",
      "latitude": 47.567301,
      "longitude": 7.606922
    },
    {
      "id": "8578143",
      "name": "Basel, Bahnhof SBB",
      "latitude": 47.548284,
      "longitude": 7.590297
    },
    {
      "id": "8588780",
      "name": "Basel, Schifflände",
      "latitude": 47.559197,
      "longitude": 7.587166
    },
    {
      "id": "8500237",
      "name": "Basel, Bankverein",
      "latitude": 47.553606,
      "longitude": 7.592251
    },
    {
      "id": "8500073",
      "name": "Basel, Aeschenplatz",
      "latitude": 47.5513,
      "longitude": 7.594862
    },
    {
      "id": "8500897",
      "name": "Basel, Barfüsserplatz",
      "latitude": 47.55459,
      "longitude": 7.589066
    },
    {
      "id": "8500193",
      "name": "Basel, Markthalle",
      "latitude": 47.548874,
      "longitude": 7.586008
    },
    {
      "id": "8589360",
      "name": "Basel, Schützenhaus",
      "latitude": 47.553202,
      "longitude": 7.577105
    },
    {
      "id": "8588775",
      "name": "Basel, Marktplatz",
      "latitude": 47.558108,
      "longitude": 7.587614
    }
  ]
}
```

---

##### Response Fields

| Field       | Type   | Description                  |
|-------------|--------|------------------------------|
| `id`        | String | Unique station identifier    |
| `name`      | String | Official station name        |
| `latitude`  | Number | Station latitude coordinate  |
| `longitude` | Number | Station longitude coordinate |

---

#### Error Responses

##### 400 — Bad Request

Occurs when the query parameter is missing, empty, or blank.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "query: Query cannot be blank",
  "timestamp": "2026-05-22T00:25:20.152Z"
}
```

---

##### 401 — Unauthorized

Occurs when the request does not contain a valid JWT token or the token has expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-22T00:33:35.539Z"
}
```

---

##### 404 — Not Found

Occurs when no stations match the provided query.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "No stations found with the name: sagmade",
  "timestamp": "2026-05-22T00:34:33.657Z"
}
```

---

##### 502 — Bad Gateway

Occurs when the external Transport API rejects the request.

```json
{
  "code": 502,
  "name": "BAD_GATEWAY",
  "description": "Api Transport rejected the request",
  "timestamp": "2026-05-22T00:34:33.657Z"
}
```

---

##### 503 — Service Unavailable

Occurs when the external Transport API is unavailable or temporarily unreachable.

```json
{
  "code": 503,
  "name": "SERVICE_UNAVAILABLE",
  "description": "Api Transport is unavailable",
  "timestamp": "2026-05-22T00:34:33.657Z"
}
```

---

#### Validation Rules

| Parameter | Validation                        |
|-----------|-----------------------------------|
| `query`   | Must not be null, empty, or blank |

---

#### Notes

* This endpoint integrates with the external Swiss Public Transport API.
* Authentication is mandatory for accessing station search functionality.
* Results may include stations, stops, terminals, and transport hubs matching the query text.
* Coordinates are returned using standard latitude and longitude values.
* Error handling includes external API availability and gateway validation.
* Timestamps are returned in ISO-8601 UTC format.



