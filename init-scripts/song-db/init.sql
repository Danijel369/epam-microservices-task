-- Schema for the Song Service database.
-- Mirrors the JPA entity com.epam.microservices.song.entity.SongEntity
-- (table "songs": assigned BIGINT id equal to the Resource ID, no identity).
-- The database itself is created by the POSTGRES_DB environment variable; this
-- script only creates tables.

CREATE TABLE songs (
    id           BIGINT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    artist       VARCHAR(100) NOT NULL,
    album        VARCHAR(100) NOT NULL,
    duration     VARCHAR(5) NOT NULL,
    release_year VARCHAR(4) NOT NULL
);
