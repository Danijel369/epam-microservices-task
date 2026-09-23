package com.epam.microservices.song.exception;

/**
 * Thrown when song metadata with the given id already exists.
 */
public class SongAlreadyExistsException extends RuntimeException {

    public SongAlreadyExistsException(long id) {
        super("Metadata for resource ID=" + id + " already exists");
    }
}
