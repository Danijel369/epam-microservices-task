package com.epam.microservices.song.exception;

/**
 * Thrown when a path-variable id is not a positive integer.
 */
public class InvalidIdException extends RuntimeException {

    public InvalidIdException(String rawValue) {
        super("Invalid value '" + rawValue + "' for ID. Must be a positive integer");
    }
}
