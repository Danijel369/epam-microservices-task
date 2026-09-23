package com.epam.microservices.song.controller;

import com.epam.microservices.song.dto.DeleteResponse;
import com.epam.microservices.song.dto.IdResponse;
import com.epam.microservices.song.dto.SongRequest;
import com.epam.microservices.song.dto.SongResponse;
import com.epam.microservices.song.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<IdResponse> create(@Valid @RequestBody SongRequest request) {
        long id = songService.create(request);
        return ResponseEntity.ok(new IdResponse(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(songService.get(id));
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponse> delete(@RequestParam("id") String id) {
        return ResponseEntity.ok(new DeleteResponse(songService.delete(id)));
    }
}
