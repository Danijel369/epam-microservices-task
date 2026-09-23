package com.epam.microservices.song.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Unified error body. {@code details} is only present for validation errors,
 * and {@code errorCode} is always serialized as a string.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String errorMessage, Map<String, String> details, String errorCode) {

    public static ErrorResponse of(String errorMessage, String errorCode) {
        return new ErrorResponse(errorMessage, null, errorCode);
    }

    public static ErrorResponse validation(String errorMessage, Map<String, String> details, String errorCode) {
        return new ErrorResponse(errorMessage, details, errorCode);
    }
}
