package com.epam.microservices.song.exception;

/**
 * Thrown when song metadata with the requested id does not exist.
 */
public class SongNotFoundException extends RuntimeException {

    public SongNotFoundException(long id) {
        super("Song metadata for ID=" + id + " not found");
    }
}
