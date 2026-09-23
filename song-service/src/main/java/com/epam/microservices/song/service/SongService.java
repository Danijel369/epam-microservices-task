package com.epam.microservices.song.service;

import com.epam.microservices.song.dto.SongRequest;
import com.epam.microservices.song.dto.SongResponse;
import com.epam.microservices.song.entity.SongEntity;
import com.epam.microservices.song.exception.SongAlreadyExistsException;
import com.epam.microservices.song.exception.SongNotFoundException;
import com.epam.microservices.song.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SongService {

    private final SongRepository repository;
    private final IdParser idParser;

    public SongService(SongRepository repository, IdParser idParser) {
        this.repository = repository;
        this.idParser = idParser;
    }

    @Transactional
    public long create(SongRequest request) {
        if (repository.existsById(request.id())) {
            throw new SongAlreadyExistsException(request.id());
        }
        SongEntity entity = new SongEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setArtist(request.artist());
        entity.setAlbum(request.album());
        entity.setDuration(request.duration());
        entity.setYear(request.year());
        return repository.save(entity).getId();
    }

    @Transactional(readOnly = true)
    public SongResponse get(String rawId) {
        long id = idParser.parsePathId(rawId);
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new SongNotFoundException(id));
    }

    @Transactional
    public List<Long> delete(String csv) {
        List<Long> requestedIds = idParser.parseCsv(csv);
        List<Long> deletedIds = new ArrayList<>();
        for (Long id : requestedIds) {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                deletedIds.add(id);
            }
        }
        return deletedIds;
    }

    private SongResponse toResponse(SongEntity entity) {
        return new SongResponse(
                entity.getId(),
                entity.getName(),
                entity.getArtist(),
                entity.getAlbum(),
                entity.getDuration(),
                entity.getYear());
    }
}
