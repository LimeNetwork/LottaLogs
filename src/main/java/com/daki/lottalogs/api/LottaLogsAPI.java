package com.daki.lottalogs.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.daki.lottalogs.api.search.SearchQuery;
import com.daki.lottalogs.api.search.SearchResult;

/**
 * Public read-only API entry point. Acquire via
 * {@code Bukkit.getServicesManager().getRegistration(LottaLogsAPI.class)}.
 */
public interface LottaLogsAPI {

    Set<String> getEnabledLogNames();

    Set<String> getAdditionalLogNames();

    boolean isLogEnabled(String logName);

    Optional<LogInfo> getLogInfo(String logName);

    List<LocalDate> getAvailableDates(String logName);

    /** @throws java.util.regex.PatternSyntaxException if query.isRegex() and query.getPattern() is not a valid regex */
    SearchResult searchLogs(SearchQuery query);

    /** Returned stream wraps an open file/reader; caller must close it (e.g. try-with-resources). */
    Stream<String> streamLogLines(String logName, LocalDate date) throws IOException;

}
