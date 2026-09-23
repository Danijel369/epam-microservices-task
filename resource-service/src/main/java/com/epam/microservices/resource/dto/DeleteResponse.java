package com.epam.microservices.resource.dto;

import java.util.List;

/**
 * Response body for a delete request: {@code {"ids": [1, 2]}}.
 */
public record DeleteResponse(List<Long> ids) {
}
