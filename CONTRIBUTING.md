# Contributing to SwissRoute

Thank you for being part of this project. This document defines the workflows, conventions, and standards every contributor must follow to keep the codebase clean, consistent, and easy to collaborate on. Please read it carefully before opening your first pull request.

---

## Table of Contents

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Clone the Repository](#clone-the-repository)
  - [Environment Configuration](#environment-configuration)
  - [Running the Full Stack](#running-the-full-stack)
- [Development Workflow](#development-workflow)
  - [Branch Strategy](#branch-strategy)
  - [Branch Naming Conventions](#branch-naming-conventions)
  - [Keeping Your Branch Up to Date](#keeping-your-branch-up-to-date)
- [Commit Conventions](#commit-conventions)
- [Database Migrations](#database-migrations)
- [Linters and Tests](#linters-and-tests)
- [Pull Requests](#pull-requests)
- [Versioning](#versioning)

---

## Getting Started

### Prerequisites

Make sure the following tools are installed on your machine before proceeding:

| Tool           | Minimum Version |
|----------------|-----------------|
| Git            | 2.x             |
| Docker         | 24.x            |
| Docker Compose | 2.x             |
| Java (JDK)     | 21              |
| Maven          | 3.9.x           |

---

### Clone the Repository

```bash
git clone https://github.com/sebas679og/swissroute-api.git
cd swissroute-api
```

---

### Environment Configuration

The project uses a `.env` file at the repository root to supply secrets and environment-specific values to Docker Compose. A template is provided — **never commit your actual `.env` file**.

1. Copy the template:

```bash
cp .env.template .env
```

2. Open `.env` and edit the values as needed. Key variables include database credentials, the base URL of the Swiss Public Transport API (`https://transport.opendata.ch/v1`), and JWT secrets.

> [!IMPORTANT]
> `.env` is already listed in `.gitignore`. If it ever shows up in `git status`, do not stage or commit it.

---

### Running the Full Stack

Once your `.env` is ready, spin up the application and its PostgreSQL database with a single command:

```bash
docker-compose up --build
```

Flyway will automatically apply all pending database migrations on startup — no manual setup is required.

To run in detached mode (background):

```bash
docker-compose up --build -d
```

To stop and remove all containers:

```bash
docker-compose down
```

To also remove volumes (wipes database data and migration history):

```bash
docker-compose down -v
```

Once running, the Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Development Workflow

### Branch Strategy

The project follows a three-tier branching model:

```
feat/* ──► dev ──► main
```

| Branch                  | Purpose                                                         |
|-------------------------|-----------------------------------------------------------------|
| `main`                  | Stable, production-ready code. Only receives merges from `dev`. |
| `dev`                   | Integration branch. All feature branches merge here first.      |
| `feat/*`, `fix/*`, etc. | Short-lived branches for individual changes.                    |

**Key rules:**
- **Never push directly to `main`.**
- All work happens on a feature/fix branch and enters the codebase through a reviewed pull request into `dev`.
- `main` is only updated from `dev` when the team decides to release.

---

### Branch Naming Conventions

Every branch must be created from `dev` and follow this naming format:

```
<prefix>/<short-description-in-kebab-case>
```

| Prefix      | When to use                                               |
|-------------|-----------------------------------------------------------|
| `feat/`     | A new feature or endpoint                                 |
| `fix/`      | A bug fix                                                 |
| `refactor/` | Code restructuring with no behaviour change               |
| `docs/`     | Documentation-only changes                                |
| `chore/`    | Maintenance tasks: dependencies, CI config, build scripts |

**Examples:**

```bash
feat/add-station-search-endpoint
feat/favorite-routes-crud
feat/search-history-persistence
fix/connection-query-date-parsing
refactor/extract-webclient-config
docs/update-api-env-variables
chore/add-flyway-migrations
```

To create a new branch:

```bash
git checkout dev
git pull origin dev
git checkout -b feat/your-feature-name
```

---

### Keeping Your Branch Up to Date

All feature branches must stay in sync with `dev` using **rebase** — not merge — to maintain a clean, linear history.

```bash
git fetch origin
git rebase origin/dev
```

If conflicts arise during rebase, resolve them file by file, then continue:

```bash
# After resolving each conflict:
git add <resolved-file>
git rebase --continue
```

To abort a rebase and return to your previous state:

```bash
git rebase --abort
```

> Rebase rewrites history. If you have already pushed your branch, you will need to force-push after rebasing:
> ```bash
> git push --force-with-lease origin feat/your-feature-name
> ```
> Use `--force-with-lease` instead of `--force` — it is safer and will refuse to overwrite if someone else has pushed to your branch.

---

## Commit Conventions

Commits must be **atomic** (one logical change per commit) and follow this format:

```
<type>: <short description in imperative mood>

<optional body — explain the why, not the what, if the change
requires additional context.>
```

| Type       | When to use                                   |
|------------|-----------------------------------------------|
| `feat`     | Introducing new functionality                 |
| `fix`      | Correcting a bug                              |
| `refactor` | Restructuring code without changing behaviour |
| `docs`     | Documentation changes only                    |
| `chore`    | Build system, dependencies, CI/CD, config     |
| `test`     | Adding or updating tests                      |
| `style`    | Formatting, whitespace — no logic changes     |

**Rules:**
- The description must be in **imperative mood**: _"add endpoint"_, not _"added endpoint"_ or _"adds endpoint"_.
- Keep the first line under **72 characters**.
- Use English or Spanish consistently **within the same branch**. Do not mix languages.
- Do not end the first line with a period.

**Examples:**

```
feat: add station search endpoint consuming /locations

Implements GET /api/stations/search with query params `query`
and optional lat/lng coordinates. Results are returned in
real time from the Swiss Transport API and are not persisted.
```

```
feat: implement favorite routes CRUD

Adds POST, GET, PUT and DELETE endpoints under /api/routes/favorites.
Each route belongs to the authenticated user and stores origin,
destination and preferred transport type.
```

```
feat: persist connection search history automatically

Every call to GET /api/connections now creates a record in
historial_busquedas with origin, destination, timestamp and
result count.
```

```
fix: correct date format sent to external connections API
```

```
refactor: extract WebClient configuration into dedicated bean
```

```
docs: document environment variables in README
```

```
chore: add Flyway migration for estaciones_favoritas table
```

```
test: add integration tests for StationBoardService
```

---

## Database Migrations

SwissRoute uses **Flyway** to manage the database schema. All migration scripts are located under `src/main/resources/db/migration` and follow the naming convention:

```
V{version}__{description}.sql
```

Flyway runs automatically when the application starts. No manual intervention is required.

### Rules for Contributors

> [!IMPORTANT]
> **Never modify or delete an already-applied migration.** Flyway stores a checksum for each applied script in the `flyway_schema_history` table. If you alter an applied migration, the application will fail on startup with a checksum mismatch error. To fix or extend a past migration, always create a **new** versioned script.

- Every database change (new table, column, index, constraint, seed data) must be accompanied by a Flyway migration script.
- The migration script must be included in the **same PR** as the feature that requires it.
- Scripts must be self-contained and idempotent where possible.
- Commit the migration script as a separate, dedicated commit using the `chore` type:

```bash
git add src/main/resources/db/migration/V5__add_column_to_users.sql
git commit -m "chore: add Flyway migration to add column to users table"
```

### Verifying Migrations Locally

After adding a new migration, start the application and check the logs for Flyway output:

```
Flyway Community Edition ... by Redgate
Database: jdbc:postgresql://localhost:5432/swissroute (PostgreSQL ...)
Successfully validated 5 migrations (execution time 00:00.012s)
Current version of schema "public": 4
Migrating schema "public" to version "5 - add column to users table"
Successfully applied 1 migration to schema "public"
```

If Flyway reports a validation error, do **not** push until it is resolved.

---

## Linters and Tests

Before opening any PR, it is **mandatory** to run Spotless locally to ensure that the code follows the project's defined style.

Applies the formatter automatically — **mandatory step before any PR**:

```bash
./mvnw spotless:apply
```

Checks that the code already complies with the format without modifying it:

```bash
./mvnw spotless:check
```

> [!IMPORTANT]
> If `spotless:check` fails in the pipeline, the PR will be rejected. Always run `spotless:apply` before pushing to avoid this.

Verifies that the code complies with predefined style rules and best practices:

```bash
./mvnw checkstyle:check
```

> [!IMPORTANT]
> If `checkstyle:check` fails, the PR will also be rejected. Make sure to fix the reported issues before pushing.

Performs static code analysis to detect quality issues, common errors, and bad practices:

```bash
./mvnw pmd:check
```

> [!IMPORTANT]
> If `pmd:check` fails, the PR will also be rejected. Make sure to fix the reported issues before pushing.

### Run Tests Only

Runs tests while skipping Spotless, Checkstyle, and PMD validations:

```bash
./mvnw -B clean verify "-Dspotless.check.skip=true" "-Dcheckstyle.skip=true" "-Dpmd.skip=true"
```

### Linters + Tests Together

Runs the formatter and all tests in a single step:

```bash
./mvnw -B clean verify
```

> [!TIP]
> It is recommended to run this command before every PR to ensure both style and tests pass successfully.

---

## Pull Requests

### From `feat/` to `dev`

1. Make sure your branch is up to date with `dev`:

```bash
git fetch origin
git rebase origin/dev
```

2. Run linters and tests locally (see previous section) before opening the PR.
3. Open the PR on GitHub targeting the `dev` branch.
4. The PR title must follow the same format as commits.
5. Briefly describe what changes were made and why.
6. Assign at least one reviewer from the team.

### From `dev` to `main`

Only project maintainers should open PRs from `dev` to `main`. Once merged, the GitHub Actions workflow will automatically build and publish the Docker image.

---

### PR Checklist

Before requesting a review, verify the following:

- [ ] Branch is rebased on top of `origin/dev`.
- [ ] All existing tests pass locally (`./mvnw verify`).
- [ ] New functionality includes at least the minimum required tests.
- [ ] Swagger/OpenAPI annotations are updated if endpoints changed.
- [ ] Database changes include a new, versioned Flyway migration script under `src/main/resources/db/migration/`.
- [ ] No existing migration scripts have been modified or deleted.
- [ ] DTOs are defined and used for all request/response payloads (no entity exposure).
- [ ] WebClient calls to the Swiss Transport API handle error responses gracefully.
- [ ] The `pom.xml` version has been bumped appropriately (see [Versioning](#versioning)).
- [ ] No secrets, `.env` files, or credentials are included in the diff.
- [ ] PR title follows the commit convention format.
- [ ] At least one reviewer has been assigned.

---

## Versioning

SwissRoute follows **[Semantic Versioning](https://semver.org/)** (`MAJOR.MINOR.PATCH`).

| Change type                        | Version bump | Example         |
|------------------------------------|--------------|-----------------|
| Bug fix                            | Patch        | `0.1.0 → 0.1.1` |
| New feature (backwards-compatible) | Minor        | `0.1.0 → 0.2.0` |
| Breaking change                    | Major        | `0.1.0 → 1.0.0` |

**Always bump the version before opening a PR.** Use the Maven Versions plugin to avoid manual edits:

```bash
./mvnw versions:set "-DnewVersion=0.2.0" "-DgenerateBackupPoms=false"
```

Then commit the version bump as a separate, dedicated commit:

```bash
git add pom.xml
git commit -m "chore: bump version to 0.2.0"
```

> Keeping the version bump in its own commit makes it easy to identify in the history and avoids cluttering meaningful change commits.
