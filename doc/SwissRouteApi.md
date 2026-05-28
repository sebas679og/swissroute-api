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
  - [Search Stations](#search-stations)
- [Connections Service](#connections-service)
  - [Search Connections](#search-connections)
- [Search History](#search-history)
  - [Get Search History](#get-search-history)
  - [Delete Search History Item](#delete-search-history-item)
  - [Clear Search History](#clear-search-history)
- [Favorite Routes](#favorite-routes)
  - [Create Favorite Route](#create-favorite-route)
  - [Get User Favorite Routes](#get-user-favorite-routes)
  - [Update Favorite Route](#update-favorite-route)
  - [Delete Favorite Route](#delete-favorite-route)
-[Favorite Stations](#favorite-stations)
  - [Create Favorite Station](#create-favorite-station)
  - [Get Favorite Stations](#get-favorite-stations)
  - [ Delete Favorite Station](#delete-favorite-station)

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

### Search Stations

Returns a list of public transport stations using either:

* Station name search
* Geographic coordinates search

#### Endpoint

```http
GET /api/stations
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

| Parameter   | Type   | Required    | Description                          |
|-------------|--------|-------------|--------------------------------------|
| `query`     | String | Conditional | Station name or partial station name |
| `latitude`  | Number | Conditional | Latitude coordinate                  |
| `longitude` | Number | Conditional | Longitude coordinate                 |

---

#### Search Modes

The endpoint supports two exclusive search modes.

##### 1. Search by Name

Uses the `query` parameter to search stations by name.

###### Example

```http
GET /api/stations?query=Basel
```

---

##### 2. Search by Coordinates

Uses geographic coordinates to search nearby stations.

###### Example

```http
GET /api/stations?latitude=47.378177&longitude=8.540192
```

---

#### Important Validation Rules

##### Allowed Combinations

| Query  | Latitude | Longitude  | Valid |
|--------|----------|------------|-------|
| ✅      | ❌        | ❌          | Yes   |
| ❌      | ✅        | ✅          | Yes   |
| ✅      | ✅        | ✅          | No    |
| ❌      | ✅        | ❌          | No    |
| ❌      | ❌        | ✅          | No    |
| ❌      | ❌        | ❌          | No    |

---

#### Coordinate Validation Rules

##### Latitude

Must be between:

-90 <= latitude <= 90

##### Longitude

Must be between:

-180 <= longitude <= 180

---

#### Successful Response

##### 200 — OK

###### Search by Name Response

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
    }
  ]
}
```

---

###### Search by Coordinates Response

When searching by coordinates, the response includes an additional `distance` field.

```json
{
  "stations": [
    {
      "id": null,
      "name": "Hauptbahnhof Zuerich, Zürich",
      "latitude": null,
      "longitude": null,
      "distance": 11
    }
  ]
}
```

---

#### Response Fields

| Field       | Type          | Description                                  |
|-------------|---------------|----------------------------------------------|
| `id`        | String        | Null / Unique station identifier             |
| `name`      | String        | Official station name                        |
| `latitude`  | Number        | Null / Station latitude coordinate           |
| `longitude` | Number        | Null / Station longitude coordinate          |
| `distance`  | Number        | Distance from provided coordinates in meters |

---

#### Error Responses

##### 400 — Bad Request

Occurs when validation rules are violated.

###### Missing Query Parameter

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "query: Query cannot be blank",
  "timestamp": "2026-05-22T00:25:20.152Z"
}
```

---

###### Missing Coordinate Pair

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "latitude and longitude must be provided together",
  "timestamp": "2026-05-22T00:25:20.152Z"
}
```

---

###### Invalid Coordinate Range

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Coordinates are out of valid range",
  "timestamp": "2026-05-22T00:25:20.152Z"
}
```

---

###### Invalid Parameter Combination

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Search by query or coordinates, but not both",
  "timestamp": "2026-05-22T00:25:20.152Z"
}
```

---

###### Missing Search Parameters

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "At least one search method is required",
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

Occurs when no stations match the provided search criteria.

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

```json id="n2u6hx"
{
  "code": 503,
  "name": "SERVICE_UNAVAILABLE",
  "description": "Api Transport is unavailable",
  "timestamp": "2026-05-22T00:34:33.657Z"
}
```

---

#### Notes

* Only one search mode can be used per request.
* `latitude` and `longitude` must always be provided together.
* Coordinates outside valid geographic ranges are rejected.
* Authentication is mandatory for accessing station search functionality.
* Results may include stations, stops, terminals, and transport hubs.
* Distance values are returned only when searching by coordinates.
* Timestamps are returned in ISO-8601 UTC format.

## Connections Service

### Search Connections

Returns available public transport connections between two stations.

### Endpoint

```http
GET /api/connections
```

### Access

Restricted — Requires authenticated user with valid JWT token.

---

### Authorization

The endpoint requires a valid JWT token in the request header.

```http
Authorization: Bearer {{JWT}}
```

---

### Query Parameters

| Parameter         | Type   | Required | Description                        |
|-------------------|--------|----------|------------------------------------|
| `from`            | String | Yes      | Origin station name                |
| `to`              | String | Yes      | Destination station name           |
| `date`            | String | No       | Travel date in `yyyy-MM-dd` format |
| `time`            | String | No       | Travel time in `HH:mm` format      |
| `transportations` | Enum[] | No       | Transport types filter             |

---

### Supported Transportation Types

The `transportations` parameter supports multiple values.

Allowed values:

* `TRAIN`
* `TRAM`
* `SHIP`
* `BUS`
* `CABLEWAY`

---

### Example Requests

#### Basic Search

```http
GET /api/connections?from=Lausanne&to=Genève
```

---

#### Search With Date and Time

```http
GET /api/connections?from=Lausanne&to=Genève&date=2026-05-25&time=21:30
```

---

#### Search With Transportation Filters

```http
GET /api/connections?from=Lausanne&to=Genève&transportations=TRAIN,BUS
```

---

### Successful Response

#### 200 — OK

```json
{
  "connections": [
    {
      "origin": "Lausanne",
      "destination": "Genève",
      "duration": "00d00:54:00",
      "products": [
        "RE33"
      ],
      "sections": [
        {
          "category": "RE",
          "number": "33",
          "operator": "SBB",
          "destination": "Genève-Aéroport",
          "departureStation": "Lausanne",
          "departureTime": "2026-05-25T21:30:00Z",
          "arrivalStation": "Genève",
          "arrivalTime": "2026-05-25T22:24:00Z",
          "platform": "7"
        }
      ]
    },
    {
      "origin": "Lausanne",
      "destination": "Genève",
      "duration": "00d00:49:00",
      "products": [
        "IR 95"
      ],
      "sections": [
        {
          "category": "IR",
          "number": "95",
          "operator": "SBB",
          "destination": "Genève",
          "departureStation": "Lausanne",
          "departureTime": "2026-05-25T21:50:00Z",
          "arrivalStation": "Genève",
          "arrivalTime": "2026-05-25T22:39:00Z",
          "platform": "8"
        }
      ]
    }
  ]
}
```

---

### Response Fields

#### Connection Fields

| Field         | Type          | Description               |
|---------------|---------------|---------------------------|
| `origin`      | String        | Origin station            |
| `destination` | String        | Destination station       |
| `duration`    | String        | Total connection duration |
| `products`    | Array<String> | Transport products used   |
| `sections`    | Array<Object> | List of route sections    |

---

#### Section Fields

| Field              | Type     | Description                      |
|--------------------|----------|----------------------------------|
| `category`         | String   | Transport category               |
| `number`           | String   | Route or line number             |
| `operator`         | String   | Transport operator               |
| `destination`      | String   | Final destination of the section |
| `departureStation` | String   | Departure station                |
| `departureTime`    | DateTime | Departure date and time          |
| `arrivalStation`   | String   | Arrival station                  |
| `arrivalTime`      | DateTime | Arrival date and time            |
| `platform`         | String   | Departure platform               |

---

### Additional Persistence Behavior

Every successful connection search is automatically persisted in the database using the `search_history` table.

The search history is associated with the authenticated user extracted from the JWT token.

---

# Persisted Search History Fields

| Field         | Description                                              |
|---------------|----------------------------------------------------------|
| `user_id`     | Authenticated user identifier obtained from JWT token    |
| `origin`      | Value provided in the `from` query parameter             |
| `destination` | Value provided in the `to` query parameter               |
| `resultCount` | Total number of connections returned by the external API |
| `searchedAt`  | Exact UTC timestamp when the search was executed         |

---

# Search History Persistence Example

| user_id                                | origin   | destination | resultCount | searchedAt                 |
|----------------------------------------|----------|-------------|-------------|----------------------------|
| `d5218cb4-bb57-4ea9-b002-f2e73505d041` | Lausanne | Genève      | 4           | `2026-05-24T00:24:29.585Z` |

---

### Error Responses

#### 400 — Bad Request

Occurs when request parameters are missing or invalid.

##### Missing Required Parameters

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "to: Destination station must not be blank; from: Origin station must not be blank",
  "timestamp": "2026-05-24T00:22:20.931Z"
}
```

---

##### Invalid Date Format

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Field 'date': invalid value '2026-05-25-34'",
  "timestamp": "2026-05-24T00:22:43.142Z"
}
```

---

##### Invalid Time Format

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Field 'time': invalid value '23:18:2321'",
  "timestamp": "2026-05-24T00:23:31.985Z"
}
```

---

##### Invalid Transportation Type

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Field 'transportations': invalid value 'buseta'",
  "timestamp": "2026-05-24T00:23:56.045Z"
}
```

---

#### 401 — Unauthorized

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

#### 404 — Not Found

Occurs when no transport connections match the provided parameters.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "No connections found for the given parameters",
  "timestamp": "2026-05-24T00:24:29.585Z"
}
```

#### 404 — User Not Found

Occurs when the authenticated user extracted from the JWT token does not exist in the system database.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "user not found",
  "timestamp": "2026-05-24T00:24:29.585Z"
}
```

---

---

#### 502 — Bad Gateway

Occurs when the external Transport API rejects the request.

```json id="b2q7pm"
{
  "code": 502,
  "name": "BAD_GATEWAY",
  "description": "Api Transport rejected the request",
  "timestamp": "2026-05-22T00:34:33.657Z"
}
```

---

#### 503 — Service Unavailable

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

### Validation Rules

| Parameter         | Validation                            |
|-------------------|---------------------------------------|
| `from`            | Must not be null, empty, or blank     |
| `to`              | Must not be null, empty, or blank     |
| `date`            | Must follow `yyyy-MM-dd` format       |
| `time`            | Must follow `HH:mm` format            |
| `transportations` | Only allowed enum values are accepted |

---

### Notes

* Authentication is mandatory for accessing this endpoint. 
* Search history is persisted only for successfully authenticated users. 
* The local persistence process occurs automatically after a successful connection search. 
* The authenticated user must exist in the local database before the search history can be stored. 
* The service integrates with the external Swiss Public Transport API. 
* Multiple transportation filters can be provided simultaneously. 
* Date and time parameters are optional and default to current system values if omitted. 
* Connection sections represent each segment of the trip. 
* All timestamps (searchedAt, timestamp, etc.) are handled and stored in ISO-8601 UTC format. 
* resultCount represents the total number of connections returned in the response payload.

---

## Search History

### Get Search History

Returns the paginated connection search history of the authenticated user.

#### Endpoint

```http
GET /api/history?page=1&size=20
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Query Parameters

| Parameter | Type    | Required | Default | Validation                  | Description    |
|-----------|---------|----------|---------|-----------------------------|----------------|
| `page`    | Integer | No       | `1`     | Minimum value: `1`          | Page number    |
| `size`    | Integer | No       | `20`    | Minimum: `1`, Maximum: `50` | Items per page |

---

#### Example Request

```http
GET /api/history?page=1&size=20
```

---

#### Successful Response

##### 200 — OK

```json
{
  "history": [
    {
      "id": "13fbb325-04cb-41a0-a5aa-9b61f55b66d3",
      "origin": "Lausanne",
      "destination": "Genève",
      "resultCount": 4,
      "searchedAt": "2026-05-25T20:57:38.072Z"
    },
    {
      "id": "5dabc735-6249-4ebc-9e94-39a1a73c3a56",
      "origin": "Lausanne",
      "destination": "Genève",
      "resultCount": 4,
      "searchedAt": "2026-05-25T20:57:28.993Z"
    },
    {
      "id": "94f93a9f-b292-4bb3-8946-c7d5ca3e359c",
      "origin": "Lausanne",
      "destination": "Genève",
      "resultCount": 4,
      "searchedAt": "2026-05-25T20:56:37.321Z"
    },
    {
      "id": "4e947c4a-0c82-4072-ae24-c9b53b42c77c",
      "origin": "Lausanne",
      "destination": "Genève",
      "resultCount": 4,
      "searchedAt": "2026-05-25T20:56:22.840Z"
    }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 4,
  "totalPages": 1
}
```

---

#### Response Fields

##### History Item Fields

| Field         | Type     | Description                           |
|---------------|----------|---------------------------------------|
| `id`          | UUID     | Search history item identifier        |
| `origin`      | String   | Origin station                        |
| `destination` | String   | Destination station                   |
| `resultCount` | Integer  | Total connections returned by the API |
| `searchedAt`  | DateTime | UTC timestamp of the search           |

---

##### Pagination Fields

| Field           | Type    | Description           |
|-----------------|---------|-----------------------|
| `page`          | Integer | Current page number   |
| `size`          | Integer | Requested page size   |
| `totalElements` | Integer | Total history records |
| `totalPages`    | Integer | Total available pages |

---

#### Error Responses

##### 400 — Bad Request

Occurs when pagination parameters are invalid.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "page: Page must be greater than or equal to 1; size: Size must be less than or equal to 50",
  "timestamp": "2026-05-25T20:58:26.857Z"
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
  "timestamp": "2026-05-25T00:33:35.539Z"
}
```

---

##### 404 — Not Found

Occurs when the authenticated user does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "User not found",
  "timestamp": "2026-05-25T00:34:33.657Z"
}
```

---

#### Notes

* Search history is scoped exclusively to the authenticated user.
* Results are returned in descending order by `searchedAt`.
* Pagination starts at page `1`.
* UTC timestamps follow ISO-8601 format.

---

### Delete Search History Item

Deletes a specific search history item belonging to the authenticated user.

#### Endpoint

```http
DELETE /api/history/{ITEM_ID}
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Path Parameters

| Parameter | Type | Required | Description                    |
|-----------|------|----------|--------------------------------|
| `ITEM_ID` | UUID | Yes      | Search history item identifier |

---

#### Example Request

```http
DELETE /api/history/13fbb325-04cb-41a0-a5aa-9b61f55b66d3
```

---

#### Successful Response

##### 204 — No Content

The history item was successfully deleted.

---

#### Error Responses

##### 400 — Bad Request

Occurs when the provided path parameter is not a valid UUID.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Invalid path parameter for: 'itemId'",
  "timestamp": "2026-05-25T20:44:37.866Z"
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

Occurs when the history item does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "Item id not found",
  "timestamp": "2026-05-24T00:24:29.585Z"
}
```

---

# Notes

* Users can only delete their own history items.
* Deletion is permanent and cannot be undone.

---

### Clear Search History

Deletes all connection search history records of the authenticated user.

#### Endpoint

```http
DELETE /api/history
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Successful Response

##### 204 — No Content

The authenticated user's search history was successfully cleared.

---

#### Error Responses

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

Occurs when the authenticated user does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "User not found",
  "timestamp": "2026-05-25T00:34:33.657Z"
}
```

---

# Notes

* Only the authenticated user's history is deleted.
* The operation permanently removes all stored search history records for the user.
* This operation cannot be undone.

---

## Favorite Routes

### Create Favorite Route

Registers a favorite route for the authenticated user.

#### Endpoint

```http
POST /api/favorite-routes
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Request Body

```json
{
  "name": "Home to work",
  "origin": "Geneve",
  "destination": "Zurich",
  "transportType": "TRAIN"
}
```

---

#### Request Fields

| Field             | Type   | Required | Description                |
|-------------------|--------|----------|----------------------------|
| `name`            | String | Yes      | Unique favorite route name |
| `origin`          | String | Yes      | Origin station             |
| `destination`     | String | Yes      | Destination station        |
| `transportType`   | Enum   | No       | Transportation type filter |

---

#### Supported Transportation Types

Allowed values for `transportType`:

* `TRAIN`
* `TRAM`
* `SHIP`
* `BUS`
* `CABLEWAY`

---

#### Validation Rules

| Field            | Validation                                   |
|------------------|----------------------------------------------|
| `name`           | Must not be null, empty, or blank            |
| `origin`         | Must not be null, empty, or blank            |
| `destination`    | Must not be null, empty, or blank            |
| `transportType`  | Optional, but must match allowed enum values |

---

#### Successful Response

##### 201 — Created

```json
{
  "id": "028abc30-423b-4b17-8bb8-87647d860b6a",
  "name": "Home to work",
  "origin": "Geneve",
  "destination": "Zurich",
  "transportType": "TRAIN",
  "createdAt": "2026-05-26T23:58:17.137Z"
}
```

---

#### Response Fields

| Field           | Type     | Description                |
|-----------------|----------|----------------------------|
| `id`            | UUID     | Favorite route identifier  |
| `name`          | String   | Favorite route name        |
| `origin`        | String   | Origin station             |
| `destination`   | String   | Destination station        |
| `transportType` | String   | Transportation type filter |
| `createdAt`     | DateTime | UTC creation timestamp     |

---

#### Error Responses

##### 400 — Bad Request

Occurs when required fields are missing or the transportation type is invalid.

###### Missing Required Fields

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "name: Name is required; origin: Origin is required; destination: Destination is required",
  "timestamp": "2026-05-26T23:58:17.137Z"
}
```

---

###### Invalid Transportation Type

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Field 'transportType': invalid value 'plane'",
  "timestamp": "2026-05-26T23:58:17.137Z"
}
```

---

##### 401 — Unauthorized

Occurs when the JWT token is malformed, invalid, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-26T23:58:17.137Z"
}
```

---

##### 404 — Not Found

Occurs when the authenticated user does not exist in the database.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "User not found",
  "timestamp": "2026-05-26T23:58:17.137Z"
}
```

---

##### 409 — Conflict

Occurs when the favorite route name already exists.

```json
{
  "code": 409,
  "name": "CONFLICT",
  "description": "Favorite route name already exists",
  "timestamp": "2026-05-26T23:58:17.137Z"
}
```

---

#### Notes

* Favorite routes are associated with the authenticated user.
* The `name` field must be unique in the database.
* Transportation type filtering is optional.
* All timestamps are returned in ISO-8601 UTC format.
* Favorite routes can later be used for quick connection searches.

---

### Get User Favorite Routes

Returns the list of favorite routes belonging to the authenticated user.

#### Endpoint

```http
GET /api/favorite-routes
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Successful Response

#### 200 — OK

```json
{
  "favoriteRoutes": [
    {
      "id": "5690feae-e7be-4be8-8d79-b52bca6ed0cf",
      "name": "Ruta1",
      "origin": "Geneve",
      "destination": "Zurich",
      "transportType": "TRAIN",
      "createdAt": "2026-05-26T19:22:48.844Z"
    },
    {
      "id": "083f72e3-4bec-469c-9325-011df84d41f4",
      "name": "Ruta2",
      "origin": "Geneve",
      "destination": "Zurich",
      "transportType": "TRAIN",
      "createdAt": "2026-05-26T19:29:13.347Z"
    },
    {
      "id": "873f2e86-6ca7-4660-a36d-38c557a9f732",
      "name": "Ruta4",
      "origin": "Geneve",
      "destination": "Zurich",
      "transportType": null,
      "createdAt": "2026-05-26T19:35:11.605Z"
    }
  ]
}
```

---

#### Response Fields

| Field           | Type          | Description                       |
|-----------------|---------------|-----------------------------------|
| `id`            | UUID          | Favorite route identifier         |
| `name`          | String        | Favorite route name               |
| `origin`        | String        | Origin station                    |
| `destination`   | String        | Destination station               |
| `transportType` | String        | Null \ Transportation type filter |
| `createdAt`     | DateTime      | UTC creation timestamp            |

---

#### Error Responses

##### 401 — Unauthorized

Occurs when the JWT token is invalid, malformed, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

##### 404 — Not Found

Occurs when the authenticated user does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "User not found",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

#### Notes

* Only favorite routes belonging to the authenticated user are returned.
* Results are ordered by creation date.
* `transportType` may be `null` when no transportation filter was configured.

---

### Update Favorite Route

Updates an existing favorite route belonging to the authenticated user.

#### Endpoint

```http
PUT /api/favorite-routes/{routeId}
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Path Parameters

| Parameter | Type | Required | Description               |
|-----------|------|----------|---------------------------|
| `routeId` | UUID | Yes      | Favorite route identifier |

---

#### Request Body

```json
{
  "name": "Home to work",
  "origin": "Basilea",
  "destination": "Berna",
  "transportType": "BUS"
}
```

---

#### Update Rules

* At least one field must be provided.
* Requests with all fields empty or null are rejected.
* Only allowed transportation enum values are accepted.
* The route name must remain unique.

---

#### Successful Response

##### 200 — OK

```json
{
  "id": "5690feae-e7be-4be8-8d79-b52bca6ed0cf",
  "name": "Home to work",
  "origin": "Basilea",
  "destination": "Berna",
  "transportType": "BUS",
  "createdAt": "2026-05-26T19:22:48.844Z"
}
```

---

#### Error Responses

##### 400 — Bad Request

Occurs when the path parameter format or request body validation fails.

###### Invalid UUID Path Parameter

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Invalid path parameter for: 'routeId'",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

###### Empty Update Request

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "At least one field must be provided for update",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

###### Invalid Transportation Type

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Field 'transportType': invalid value 'plane'",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

##### 401 — Unauthorized

Occurs when the JWT token is invalid, malformed, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

##### 404 — Not Found

Occurs when the user or favorite route does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "Favorite route not found",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

##### 409 — Conflict

Occurs when another favorite route already uses the same route name.

```json
{
  "code": 409,
  "name": "CONFLICT",
  "description": "Favorite route name already exists",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

#### Notes

* Users can update only their own favorite routes.
* Partial updates are supported.
* All timestamps are returned in ISO-8601 UTC format.

---

### Delete Favorite Route

Deletes a favorite route belonging to the authenticated user.

#### Endpoint

```http
DELETE /api/favorite-routes/{routeId}
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http id="g4x1lt"
Authorization: Bearer {{JWT}}
```

---

#### Path Parameters

| Parameter | Type | Required | Description               |
|-----------|------|----------|---------------------------|
| `routeId` | UUID | Yes      | Favorite route identifier |

---

#### Example Request

```http
DELETE /api/favorite-routes/5690feae-e7be-4be8-8d79-b52bca6ed0cf
```

---

#### Successful Response

##### 204 — No Content

The favorite route was successfully deleted.

---

#### Error Responses

##### 400 — Bad Request

Occurs when the UUID path parameter is malformed or invalid.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Invalid path parameter for: 'routeId'",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

##### 401 — Unauthorized

Occurs when the JWT token is malformed, invalid, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

##### 404 — Not Found

Occurs when the user or favorite route does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "Favorite route not found",
  "timestamp": "2026-05-26T19:22:48.844Z"
}
```

---

#### Notes

* Users can delete only their own favorite routes.
* Deletion is permanent and cannot be undone.

---

## Favorite Stations

### Create Favorite Station

Adds a station to the authenticated user's list of favorite stations.

#### Endpoint

```http
POST /api/favorite-stations
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Request Body

All request fields are required.

```json
{
  "externalStationId": "850309",
  "stationName": "Aarau"
}
```

---

#### Request Fields

| Field               | Type   | Required | Description                                    |
|---------------------|--------|----------|------------------------------------------------|
| `externalStationId` | String | Yes      | External station identifier from Transport API |
| `stationName`       | String | Yes      | Station name                                   |

---

#### Validation Rules

| Field               | Validation                        |
|---------------------|-----------------------------------|
| `externalStationId` | Must not be null, empty, or blank |
| `stationName`       | Must not be null, empty, or blank |
| `externalStationId` | Must be unique per user           |

---

#### Successful Response

##### 201 — Created

```json
{
  "externalStationId": "850309",
  "stationName": "Aarau",
  "createdAt": "2026-05-28T17:18:54.723Z"
}
```

---

##### Response Fields

| Field               | Type     | Description                 |
|---------------------|----------|-----------------------------|
| `externalStationId` | String   | External station identifier |
| `stationName`       | String   | Station name                |
| `createdAt`         | DateTime | UTC creation timestamp      |

---

#### Error Responses

##### 400 — Bad Request

Occurs when request validation fails.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "externalStationId: External station id is required; stationName: Station name is required",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

##### 401 — Unauthorized

Occurs when authentication is missing, invalid, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

##### 404 — Not Found

Occurs when the authenticated user does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "User not found",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

##### 409 — Conflict

Occurs when the station is already registered as favorite.

```json
{
  "code": 409,
  "name": "CONFLICT",
  "description": "externalStationId already registered",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

#### Notes

* Favorite stations are associated with the authenticated user.
* Duplicate station registrations are not allowed.
* All timestamps are returned in ISO-8601 UTC format.

---

### Get Favorite Stations

Returns all favorite stations belonging to the authenticated user.

#### Endpoint

```http
GET /api/favorite-stations
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

#### Successful Response

#### 200 — OK

```json
{
  "favoriteStations": [
    {
      "externalStationId": "8430555",
      "stationName": "Aarau",
      "createdAt": "2026-05-28T16:36:08.181Z"
    },
    {
      "externalStationId": "84503055",
      "stationName": "Aarau",
      "createdAt": "2026-05-28T16:36:00.650Z"
    },
    {
      "externalStationId": "8503059",
      "stationName": "Aarau",
      "createdAt": "2026-05-28T16:35:45.563Z"
    }
  ]
}
```

---

#### Response Fields

| Field               | Type     | Description                 |
|---------------------|----------|-----------------------------|
| `externalStationId` | String   | External station identifier |
| `stationName`       | String   | Station name                |
| `createdAt`         | DateTime | UTC creation timestamp      |

---

#### Error Responses

##### 401 — Unauthorized

Occurs when authentication is missing, invalid, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-28T16:36:08.181Z"
}
```

---

##### 404 — Not Found

Occurs when the authenticated user does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "User not found",
  "timestamp": "2026-05-28T16:36:08.181Z"
}
```

---

#### Notes

* Only stations belonging to the authenticated user are returned.
* Results are ordered by creation date.
* Timestamps are returned in ISO-8601 UTC format.

---

### Delete Favorite Station

Deletes a favorite station belonging to the authenticated user.

#### Endpoint

```http
DELETE /api/favorite-stations/{externalStationId}
```

#### Access

Restricted — Requires authenticated user with valid JWT token.

---

#### Authorization

```http
Authorization: Bearer {{JWT}}
```

---

# Path Parameters

| Parameter           | Type   | Required | Description                 |
|---------------------|--------|----------|-----------------------------|
| `externalStationId` | String | Yes      | External station identifier |

---

#### Example Request

```http
DELETE /api/favorite-stations/850309
```

---

#### Successful Response

##### 204 — No Content

The favorite station was successfully deleted.

---

#### Error Responses

##### 400 — Bad Request

Occurs when the path parameter is invalid or malformed.

```json
{
  "code": 400,
  "name": "BAD_REQUEST",
  "description": "Invalid path parameter for: 'externalStationId'",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

##### 401 — Unauthorized

Occurs when authentication is missing, invalid, or expired.

```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Authentication required or token expired",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

##### 404 — Not Found

Occurs when the user or station identifier does not exist.

```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "Favorite station not found",
  "timestamp": "2026-05-28T17:18:54.723Z"
}
```

---

#### Notes

* Users can delete only their own favorite stations.
* Deletion is permanent and cannot be undone.
* All timestamps are returned in ISO-8601 UTC format.

---
