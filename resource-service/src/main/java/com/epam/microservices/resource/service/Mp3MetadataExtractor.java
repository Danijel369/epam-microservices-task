package com.epam.microservices.resource.service;

import com.epam.microservices.resource.dto.Mp3Tags;
import com.epam.microservices.resource.exception.InvalidMp3Exception;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Extracts MP3 tags with Apache Tika's {@link Mp3Parser}. All tags are passed
 * through unchanged except {@code duration}, which is converted from seconds to
 * mm:ss format.
 */
@Component
public class Mp3MetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(Mp3MetadataExtractor.class);
    private static final String MP3_MEDIA_TYPE = "audio/mpeg";

    private final Tika tika = new Tika();

    public Mp3Tags extract(byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidMp3Exception("Invalid MP3 file: request body is empty");
        }
        if (!MP3_MEDIA_TYPE.equals(detectContentType(data))) {
            throw new InvalidMp3Exception("Invalid MP3 file: the uploaded content is not a valid MP3");
        }

        Metadata metadata = new Metadata();
        try (InputStream stream = new ByteArrayInputStream(data)) {
            // BodyContentHandler(-1) disables the write limit so parsing never aborts.
            new Mp3Parser().parse(stream, new BodyContentHandler(-1), metadata, new ParseContext());
        } catch (Exception e) {
            throw new InvalidMp3Exception("Invalid MP3 file: the uploaded content is not a valid MP3");
        }

        String name = metadata.get(TikaCoreProperties.TITLE);
        String artist = metadata.get(XMPDM.ARTIST);
        String album = metadata.get(XMPDM.ALBUM);
        String duration = toMmSs(metadata.get(XMPDM.DURATION));
        String year = extractYear(metadata);

        log.info("Extracted MP3 tags: name='{}', artist='{}', album='{}', duration='{}', year='{}'",
                name, artist, album, duration, year);
        return new Mp3Tags(name, artist, album, duration, year);
    }

    /**
     * Detects the real media type of the bytes from their content (magic bytes),
     * independent of any client-supplied header, so that garbage sent with an
     * {@code audio/mpeg} header is recognised as not being an MP3.
     */
    private String detectContentType(byte[] data) {
        try {
            return tika.detect(data);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a duration expressed in seconds (Tika reports it as a decimal
     * number of seconds) to mm:ss with leading zeros.
     */
    private String toMmSs(String rawSeconds) {
        if (rawSeconds == null || rawSeconds.isBlank()) {
            return null;
        }
        double seconds;
        try {
            seconds = Double.parseDouble(rawSeconds.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        long total = (long) Math.floor(seconds);
        long minutes = total / 60;
        long secs = total % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Uses the release date tag as the year, passed through unchanged.
     */
    private String extractYear(Metadata metadata) {
        return metadata.get(XMPDM.RELEASE_DATE);
    }
}
