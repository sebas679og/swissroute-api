# SwissRoute — Backend for Public Transport Trip Planning and Tracking

---

## Table of Contents

- [Problem Statement](#problem-statement)
- [Proposed Solution](#proposed-solution)
- [Project Justification](#project-justification)
- [Technology Stack](#technology-stack)
- [Authentication Service](#authentication-service)
  - [Register User](#register-user)

---

## Problem Statement

Modern public transportation systems generate a large amount of route, schedule, and connection data. However, accessing and organizing this information efficiently remains a challenge for many users. Travelers often need to consult multiple platforms to plan routes, verify schedules, and manage their preferred trips, resulting in a fragmented and inconvenient experience.

Additionally, most public transport APIs provide raw transportation data but lack personalized features such as favorite route management, travel history tracking, and centralized trip planning. This creates an opportunity to build a backend system capable of transforming public transportation data into a structured, scalable, and user-oriented service.

SwissRoute addresses this problem by providing a robust backend API that integrates with the Swiss Public Transport API and offers advanced functionalities for authenticated users, including connection searches, favorite route storage, timetable consultation, and travel history management.

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
| -------------------- | --------------------------- |
| Programming Language | Java 21+                    |
| Framework            | Spring Boot 3.5.x           |
| Database             | PostgreSQL                  |
| ORM                  | Spring Data JPA / Hibernate |
| HTTP Client          | WebClient                   |
| Documentation        | Swagger / OpenAPI           |
| Build Tool           | Maven                       |

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
| ---------- | ------ | -------- | ----------------------------------------- |
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
| ---------- | -------------------------------------------------- |
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

