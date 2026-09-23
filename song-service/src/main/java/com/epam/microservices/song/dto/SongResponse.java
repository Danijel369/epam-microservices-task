package com.epam.microservices.song.dto;

/**
 * Response body for reading song metadata.
 */
public record SongResponse(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year) {
}
