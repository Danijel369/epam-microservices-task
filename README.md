# Microservices: Resource Service & Song Service

A two-service system for MP3 file processing and song-metadata management.

- **Resource Service** (port `8081`) — stores MP3 files, extracts their tags with Apache Tika,
  and forwards the metadata to the Song Service.
- **Song Service** (port `8082`) — manages song metadata, keyed by the Resource ID.

## Tech stack

- Java 17, Spring Boot 3.4.1, Maven (no Lombok, no Kotlin)
- PostgreSQL 17 (Alpine) — one database per service, started via Docker Compose
- Hibernate `ddl-auto=update` for schema management (no Flyway/Liquibase, no `schema.sql`/`data.sql`)
- Apache Tika 2.9.2 (`tika-core` + `tika-parser-audiovideo-module`) for MP3 tag extraction
- Spring `RestClient` for service-to-service calls

## Project layout

```
epam-microservices-task/
├── resource-service/     # MP3 storage & processing (port 8081, DB localhost:5432/resource_db)
│   ├── src/
│   └── pom.xml
├── song-service/         # Song metadata (port 8082, DB localhost:5433/song_db)
│   ├── src/
│   └── pom.xml
├── compose.yaml          # starts ONLY the two PostgreSQL databases
└── .gitignore
```

The services run **locally** (not in Docker). Only the databases run in containers.

## Prerequisites

- JDK 17+
- Maven 3.9+
- Docker (with Docker Compose) — this project was developed with OrbStack on macOS

## Running the system

1. **Start the databases:**

   ```bash
   docker compose up -d
   ```

   This starts `resource-db` on `localhost:5432` and `song-db` on `localhost:5433`.
   The database storage is ephemeral, so `docker compose down` fully resets the data.

2. **Build both services:**

   ```bash
   mvn -f song-service/pom.xml -DskipTests package
   mvn -f resource-service/pom.xml -DskipTests package
   ```

3. **Run the services** (start the Song Service first, then the Resource Service):

   ```bash
   java -jar song-service/target/song-service-1.0.0.jar
   java -jar resource-service/target/resource-service-1.0.0.jar
   ```

   Or, during development:

   ```bash
   mvn -f song-service/pom.xml spring-boot:run
   mvn -f resource-service/pom.xml spring-boot:run
   ```

4. **Reset the databases** (clean slate for a fresh test run):

   ```bash
   docker compose down && docker compose up -d
   # then restart both services
   ```

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

All 382 assertions across 33 requests pass.
