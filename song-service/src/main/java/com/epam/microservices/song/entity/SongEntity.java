package com.epam.microservices.song.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "songs")
public class SongEntity {

    // Assigned identifier (the Resource ID); not generated.
    @Id
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "artist", nullable = false, length = 100)
    private String artist;

    @Column(name = "album", nullable = false, length = 100)
    private String album;

    @Column(name = "duration", nullable = false, length = 5)
    private String duration;

    @Column(name = "release_year", nullable = false, length = 4)
    private String year;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
