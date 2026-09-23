package com.epam.microservices.resource.dto;

/**
 * Payload sent to the Song Service to store metadata extracted from an MP3 file.
 * Field names and values are passed through unchanged from the MP3 tags,
 * except {@code duration}, which is converted from seconds to mm:ss.
 */
public record SongMetadataRequest(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year) {
}
