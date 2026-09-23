package com.epam.microservices.resource.exception;

/**
 * Thrown when an uploaded file does not have the {@code audio/mpeg} content type.
 */
public class InvalidFileFormatException extends RuntimeException {

    public InvalidFileFormatException(String contentType) {
        super("Invalid file format: " + contentType + ". Only MP3 files are allowed");
    }
}
