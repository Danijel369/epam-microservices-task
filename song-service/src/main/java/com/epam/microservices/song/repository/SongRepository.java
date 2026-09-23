package com.epam.microservices.song.repository;

import com.epam.microservices.song.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<SongEntity, Long> {
}
