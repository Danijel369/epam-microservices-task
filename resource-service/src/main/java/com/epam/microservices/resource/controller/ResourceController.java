package com.epam.microservices.resource.controller;

import com.epam.microservices.resource.dto.DeleteResponse;
import com.epam.microservices.resource.dto.IdResponse;
import com.epam.microservices.resource.service.ResourceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<IdResponse> upload(
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
            @RequestBody byte[] data) {
        long id = resourceService.upload(data, contentType);
        return ResponseEntity.ok(new IdResponse(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable String id) {
        byte[] data = resourceService.getData(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(data);
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponse> deleteResources(@RequestParam("id") String id) {
        return ResponseEntity.ok(new DeleteResponse(resourceService.delete(id)));
    }
}
