# Microservices: Resource Service & Song Service

A microservices system for MP3 file processing and song-metadata management, with a
**Eureka service registry** for service registration, discovery and client-side load balancing.

- **Eureka Server** (port `8761`) — the service registry. All services register here and discover
  each other through it.
- **Resource Service** (port `8081`) — stores MP3 files, extracts their tags with Apache Tika,
  and forwards the metadata to the Song Service, which it **discovers through Eureka** (not a fixed URL).
- **Song Service** (ports `8082`/`8083`) — manages song metadata, keyed by the Resource ID. Runs as
  **two instances** so the Resource Service load-balances across them.

## Architecture

```
                         ┌──────────────────────┐
                         │   Eureka Server 8761 │  (registry + dashboard)
                         └──────────▲───────────┘
              register / discover   │   register (heartbeats)
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
┌───────┴────────┐        ┌─────────┴─────────┐       ┌─────────┴─────────┐
│ resource-service│  LB   │  song-service #1  │       │  song-service #2  │
│      8081       ├──────▶│       8082        │       │       8083        │
└───────┬────────┘  http://song-service       └─────────┬─────────┘
        │                        (resolved via Eureka)   │
┌───────┴────────┐                              ┌────────┴────────┐
│  resource-db   │                              │     song-db     │
│  postgres 5432 │                              │  postgres 5433  │
└────────────────┘                              └─────────────────┘
```

The Resource Service calls the Song Service through a `@LoadBalanced RestClient` whose base URL is the
logical id `http://song-service`; Spring Cloud LoadBalancer resolves that id against the Eureka
registry and round-robins across the registered instances.

## Tech stack

- Java 17, Spring Boot 3.4.1, Maven (no Lombok, no Kotlin)
- Spring Cloud 2024.0.1 (Moorgate): Netflix Eureka **server** + **client**, and Spring Cloud
  LoadBalancer (pulled in by the Eureka client starter) for client-side load balancing
- PostgreSQL 17 (Alpine) — one database per service, run in Docker
- Schema created by SQL init scripts inside the DB containers; Hibernate `ddl-auto=none`
  (no Flyway/Liquibase, no `schema.sql`/`data.sql`)
- Apache Tika 2.9.2 (`tika-core` + `tika-parser-audiovideo-module`) for MP3 tag extraction
- Spring `RestClient` (load-balanced) for service-to-service calls
- Two-stage Docker builds (Temurin Alpine), orchestrated with a single `compose.yaml`

## Project layout

```
epam-microservices-task/
├── init-scripts/
│   ├── resource-db/init.sql   # CREATE TABLE resources (...)
│   └── song-db/init.sql       # CREATE TABLE songs (...)
├── eureka-server/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile             # two-stage build, EXPOSE 8761
│   └── .dockerignore
├── resource-service/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile             # two-stage build, EXPOSE 8081
│   └── .dockerignore
├── song-service/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile             # two-stage build, EXPOSE 8082 (scaled to 2 instances)
│   └── .dockerignore
├── compose.yaml               # Eureka + DBs + both services, single-command bring-up
├── .env                       # DB names/users/passwords + EUREKA_URL
└── .gitignore
```

## Prerequisites

- Docker with Docker Compose (developed with **OrbStack** on macOS)
- For local mode only: JDK 17+ and Maven 3.9+

> **Apple Silicon note:** the task's recommended base images
> (`maven:3.9-eclipse-temurin-17-alpine`, `eclipse-temurin:17-jre-alpine`) are published for
> `linux/amd64` only, so on an ARM Mac the two service containers run under emulation. This is made
> explicit with `platform: linux/amd64` on the service blocks in `compose.yaml`; everything works
> unchanged, builds are just a little slower than native. (Verify emulation with
> `docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`.)

## Running the system

The **same** `application.yaml` works in both modes with no profile switching: container-specific
values are injected as environment variables in Docker, and fall back to `localhost` defaults when
run locally.

### Docker mode (everything in containers)

Build and start Eureka, both databases and both services — with the Song Service scaled to **two
instances** — with a single command. Either of these works and produces the same result (2 song-service
replicas on fixed host ports 8082 and 8083):

```bash
# Option 2 (fixed ports) — replicas are declared in compose.yaml (deploy.replicas: 2)
docker compose up -d --build

# Explicit scale flag — equivalent, and overrides deploy.replicas
docker compose up -d --build --scale song-service=2
```

- `eureka-server` → `localhost:8761` (dashboard)
- `resource-db` → `localhost:5432`, `song-db` → `localhost:5433`
- `resource-service` → `localhost:8081`
- `song-service` → **two** instances on `localhost:8082` and `localhost:8083` (both container port 8082)
- Postman keeps using `song_service_url=http://localhost:8082`; the Resource Service reaches the Song
  Service internally through Eureka, load-balancing across both replicas.
- Databases are created by `POSTGRES_DB`; tables are created by the mounted `init-scripts/*/init.sql`.
- No data volume is used, so `docker compose down` fully resets the databases.

Open **http://localhost:8761** and wait until it lists **1 instance of RESOURCE-SERVICE** and
**2 instances of SONG-SERVICE** before running the API tests (registration takes a few seconds).

Check status and stop:

```bash
docker compose ps          # 6 containers: eureka, resource-service, 2× song-service, 2 DBs
docker compose down
```

### Local mode (services on the host, DBs in Docker)

Start only the databases in Docker, then run the registry and the services from the host **in this
order** (Eureka first so the clients can register with it):

```bash
docker compose up -d resource-db song-db

mvn -f eureka-server/pom.xml spring-boot:run     # 1) registry on :8761
mvn -f song-service/pom.xml spring-boot:run      # 2) registers as SONG-SERVICE
mvn -f resource-service/pom.xml spring-boot:run  # 3) registers, discovers song-service
```

The services use the `localhost:5432` / `localhost:5433` defaults for the databases and
`http://localhost:8761/eureka` for the registry, all baked into `application.yaml` — so no
configuration change or Spring profile is needed. The Resource Service still reaches the Song Service
by its logical id `http://song-service`, resolved through Eureka. Open **http://localhost:8761** to
confirm both services are registered before testing.

## API summary

### Resource Service (`http://localhost:8081`)

| Method | Path                | Request                       | Success response          |
|--------|---------------------|-------------------------------|---------------------------|
| POST   | `/resources`        | `Content-Type: audio/mpeg`, binary MP3 body | `200 {"id": 1}` |
| GET    | `/resources/{id}`   | —                             | `200` binary (`audio/mpeg`) |
| DELETE | `/resources?id=1,2` | CSV of ids (max 200 chars)    | `200 {"ids": [1, 2]}`     |

### Song Service (`http://localhost:8082`)

| Method | Path            | Request                        | Success response          |
|--------|-----------------|--------------------------------|---------------------------|
| POST   | `/songs`        | JSON metadata body             | `200 {"id": 1}`           |
| GET    | `/songs/{id}`   | —                              | `200` metadata JSON       |
| DELETE | `/songs?id=1,2` | CSV of ids (max 200 chars)     | `200 {"ids": [1, 2]}`     |

### Error format

All errors share a unified body; `errorCode` is always a **string**, and `details` appears only
for validation errors:

```json
{ "errorMessage": "Resource with ID=1 not found", "errorCode": "404" }
```

```json
{
  "errorMessage": "Validation error",
  "details": { "duration": "Duration must be in mm:ss format with leading zeros" },
  "errorCode": "400"
}
```

## API testing (Postman / Newman)

The provided Postman collection (`postman/introduction_to_microservices.postman_collection.json`)
validates all endpoints and error cases. To run it headless:

```bash
npx newman run postman/introduction_to_microservices.postman_collection.json \
  --env-var resource_service_url=http://localhost:8081 \
  --env-var song_service_url=http://localhost:8082 \
  --working-dir postman
```

> **Note on the MP3 file path:** the two file-upload requests in the collection ship with an empty
> file `src`. Newman cannot resolve an empty path, so for a headless run, copy the collection to a
> temporary location and set the file `src` of those two requests to
> `postman/mp3/valid-sample-with-required-tags.mp3` (an absolute path), then run Newman on the copy.
> The original collection under `postman/` is never modified. In the Postman desktop app you simply
> select the MP3 file for those requests via the file picker.

All 382 assertions across 33 requests pass in **both** Docker mode and local mode.
