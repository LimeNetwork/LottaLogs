package com.daki.lottalogs.api.search;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public record LogEntry(
        LocalDate date,
        String rawLine,
        Map<String, String> parsedArgs
) {

    public LogEntry {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(rawLine, "rawLine must not be null");
        Objects.requireNonNull(parsedArgs, "parsedArgs must not be null");
        parsedArgs = Map.copyOf(parsedArgs);
    }

}
