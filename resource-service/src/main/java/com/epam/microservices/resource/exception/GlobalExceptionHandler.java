package com.epam.microservices.resource.exception;

import com.epam.microservices.resource.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileFormat(InvalidFileFormatException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(InvalidIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidId(InvalidIdException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(InvalidCsvTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCsvToken(InvalidCsvTokenException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(CsvTooLongException.class)
    public ResponseEntity<ErrorResponse> handleCsvTooLong(CsvTooLongException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return json(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), String.valueOf(HttpStatus.NOT_FOUND.value())));
    }

    /**
     * Passes an error from the Song Service through unchanged: same status, same JSON body.
     */
    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<String> handleDownstream(DownstreamException ex) {
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getBody());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return json(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Internal server error",
                        String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return json(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }

    private ResponseEntity.BodyBuilder json(HttpStatus status) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON);
    }
}
