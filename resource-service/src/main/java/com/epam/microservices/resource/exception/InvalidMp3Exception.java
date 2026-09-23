package com.epam.microservices.resource.exception;

/**
 * Thrown when the uploaded body is not a usable MP3 (empty body or content that
 * is not actually an MP3). Mapped to {@code 400 Bad Request}.
 */
public class InvalidMp3Exception extends RuntimeException {

    public InvalidMp3Exception(String message) {
        super(message);
    }
}
