package com.epam.microservices.resource.exception;

/**
 * Carries an error returned by the Song Service so that the Resource Service can
 * pass the downstream status and body through unchanged. Throwing this inside a
 * transactional method also rolls back the resource changes.
 */
public class DownstreamException extends RuntimeException {

    private final int status;
    private final String body;

    public DownstreamException(int status, String body) {
        super("Song Service responded with status " + status);
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
