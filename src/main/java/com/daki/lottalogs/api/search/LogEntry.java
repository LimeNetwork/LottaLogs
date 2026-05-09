package com.daki.lottalogs.api.search;

import java.time.LocalDate;
import java.util.Map;

public record LogEntry(
        LocalDate date,
        String rawLine,
        Map<String, String> parsedArgs
) {

    public LogEntry {
        parsedArgs = Map.copyOf(parsedArgs);
    }

}
