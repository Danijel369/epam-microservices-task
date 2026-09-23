package com.epam.microservices.song.exception;

import com.epam.microservices.song.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return json(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validation("Validation error", details,
                        String.valueOf(HttpStatus.BAD_REQUEST.value())));
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

    @ExceptionHandler(SongNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SongNotFoundException ex) {
        return json(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), String.valueOf(HttpStatus.NOT_FOUND.value())));
    }

    @ExceptionHandler(SongAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(SongAlreadyExistsException ex) {
        return json(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getMessage(), String.valueOf(HttpStatus.CONFLICT.value())));
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
