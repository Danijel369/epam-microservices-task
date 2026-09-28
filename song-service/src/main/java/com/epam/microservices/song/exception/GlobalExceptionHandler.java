package com.epam.microservices.song.exception;

import com.epam.microservices.song.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    /**
     * A known path hit with the wrong HTTP verb (e.g. {@code GET /songs}) is a
     * client mistake, not a server fault — answer 405 instead of 500.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return json(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of("Method " + ex.getMethod() + " is not supported for this endpoint",
                        String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value())));
    }

    /**
     * A required query parameter left off is a bad request, not a server fault.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return badRequest("Required request parameter '" + ex.getParameterName() + "' is missing");
    }

    /**
     * An unknown path (no handler / no static resource) is a 404, not a 500.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleUnknownPath(Exception ex) {
        return json(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("Endpoint not found",
                        String.valueOf(HttpStatus.NOT_FOUND.value())));
    }

    /**
     * The client's {@code Accept} header excludes every representation we can
     * produce — that is a 406, not a server fault.
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return json(HttpStatus.NOT_ACCEPTABLE)
                .body(ErrorResponse.of("Requested media type is not acceptable",
                        String.valueOf(HttpStatus.NOT_ACCEPTABLE.value())));
    }

    /**
     * An empty or non-JSON request body (e.g. {@code POST /songs} with no bytes)
     * fails in the message converter before reaching the controller; map it to a
     * clear 400 rather than 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return badRequest("Request body is missing or is not valid JSON");
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
