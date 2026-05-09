package com.daki.lottalogs.api.search;

import java.util.List;

public record SearchResult(
        String logName,
        int matchCount,
        List<LogEntry> entries,
        boolean truncated
) {

    public SearchResult {
        entries = List.copyOf(entries);
    }

}
