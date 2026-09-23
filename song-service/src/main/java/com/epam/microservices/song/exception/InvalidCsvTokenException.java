package com.epam.microservices.song.exception;

/**
 * Thrown when a token in the delete CSV is not a positive integer.
 */
public class InvalidCsvTokenException extends RuntimeException {

    public InvalidCsvTokenException(String token) {
        super("Invalid ID format: '" + token + "'. Only positive integers are allowed");
    }
}
