package com.epam.microservices.resource.exception;

/**
 * Thrown when the delete CSV string exceeds the maximum allowed length.
 */
public class CsvTooLongException extends RuntimeException {

    public CsvTooLongException(int actualLength, int maxLength) {
        super("CSV string is too long: received " + actualLength
                + " characters, maximum allowed is " + maxLength);
    }
}
