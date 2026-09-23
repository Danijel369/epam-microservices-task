package com.epam.microservices.song.service;

import com.epam.microservices.song.exception.CsvTooLongException;
import com.epam.microservices.song.exception.InvalidCsvTokenException;
import com.epam.microservices.song.exception.InvalidIdException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses and validates song ids. Only positive integers are accepted; parsing
 * is overflow-safe (values above {@link Integer#MAX_VALUE} are rejected without
 * throwing {@link NumberFormatException} to the caller).
 */
@Component
public class IdParser {

    private static final int MAX_CSV_LENGTH = 200;
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    public long parsePathId(String rawValue) {
        Long value = toPositiveInt(rawValue);
        if (value == null) {
            throw new InvalidIdException(rawValue);
        }
        return value;
    }

    public List<Long> parseCsv(String csv) {
        String value = csv == null ? "" : csv;
        if (value.length() > MAX_CSV_LENGTH) {
            throw new CsvTooLongException(value.length(), MAX_CSV_LENGTH);
        }
        List<Long> ids = new ArrayList<>();
        for (String token : value.split(",", -1)) {
            String trimmed = token.trim();
            Long parsed = toPositiveInt(trimmed);
            if (parsed == null) {
                throw new InvalidCsvTokenException(trimmed);
            }
            ids.add(parsed);
        }
        return ids;
    }

    private Long toPositiveInt(String s) {
        if (s == null || !DIGITS.matcher(s).matches()) {
            return null;
        }
        try {
            long value = Long.parseLong(s);
            if (value <= 0 || value > Integer.MAX_VALUE) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
