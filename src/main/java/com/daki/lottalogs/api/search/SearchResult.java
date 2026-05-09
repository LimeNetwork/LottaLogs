package com.daki.lottalogs.api.search;

import java.util.List;
import java.util.Objects;

public record SearchResult(
        String logName,
        int matchCount,
        List<LogEntry> entries,
        boolean truncated
) {

    public SearchResult {
        Objects.requireNonNull(logName, "logName must not be null");
        Objects.requireNonNull(entries, "entries must not be null");
        entries = List.copyOf(entries);
    }

}
