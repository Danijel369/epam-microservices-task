package com.epam.microservices.song.service;

import com.epam.microservices.song.dto.SongRequest;
import com.epam.microservices.song.dto.SongResponse;
import com.epam.microservices.song.entity.SongEntity;
import com.epam.microservices.song.exception.SongAlreadyExistsException;
import com.epam.microservices.song.exception.SongNotFoundException;
import com.epam.microservices.song.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SongService {

    private static final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository repository;
    private final IdParser idParser;
    // Host name of this JVM. In Docker it is the container id, so it differs per
    // song-service replica and makes client-side load balancing visible in logs.
    private final String instanceId = resolveInstanceId();

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
        long id = repository.save(entity).getId();
        log.info("[{}] Created song metadata id={}", instanceId, id);
        return id;
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
        log.info("[{}] Deleted song metadata ids={}", instanceId, deletedIds);
        return deletedIds;
    }

    private static String resolveInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
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
