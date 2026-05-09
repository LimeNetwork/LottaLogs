package com.daki.lottalogs.api.search;

import java.util.List;
import java.util.Objects;

/**
 * @param matchCount number of returned entries (equals {@code entries.size()}; if {@code truncated} is true, the actual total may be larger)
 */
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
