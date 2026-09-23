package com.epam.microservices.resource.exception;

/**
 * Thrown when a resource with the requested id does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(long id) {
        super("Resource with ID=" + id + " not found");
    }
}
