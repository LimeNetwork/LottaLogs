package com.daki.lottalogs.api.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.bukkit.Location;

import com.daki.lottalogs.LottaLogs;
import com.daki.lottalogs.api.LogInfo;
import com.daki.lottalogs.api.LottaLogsAPI;
import com.daki.lottalogs.api.search.LogEntry;
import com.daki.lottalogs.api.search.SearchMode;
import com.daki.lottalogs.api.search.SearchQuery;
import com.daki.lottalogs.api.search.SearchResult;
import com.daki.lottalogs.logs.Log;
import com.daki.lottalogs.other.Logging;

public final class LottaLogsAPIImpl implements LottaLogsAPI {

    private static final String LOGS_DIR = "logs";
    private static final String COMPRESSED_LOGS_DIR = "compressed-logs";
    private static final String LOG_EXT = ".txt";
    private static final String GZ_EXT = ".txt.gz";

    @Override
    public Set<String> getEnabledLogNames() {
        return Logging.getCachedLogs().values().stream()
                .filter(Log::isEnabled)
                .map(Log::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> getAdditionalLogNames() {
        return Set.copyOf(Logging.getAdditionalLogNames());
    }

    @Override
    public boolean isLogEnabled(String logName) {
        Log log = Logging.getCachedLogs().get(logName);
        return log != null && log.isEnabled();
    }

    @Override
    public Optional<LogInfo> getLogInfo(String logName) {
        Log log = Logging.getCachedLogs().get(logName);
        if (log == null) {
            return Optional.empty();
        }
        List<String> args = new ArrayList<>(log.getArguments().keySet());
        return Optional.of(new LogInfo(
                log.getName(),
                log.isEnabled(),
                log.getDaysOfLogsToKeep(),
                log.getBlacklistedStrings(),
                args
        ));
    }

    @Override
    public List<LocalDate> getAvailableDates(String logName) {
        Objects.requireNonNull(logName, "logName must not be null");

        boolean isAdditional = Logging.getAdditionalLogNames().contains(logName);
        TreeSet<LocalDate> dates = new TreeSet<>();

        if (isAdditional) {
            File dir = additionalLogDirectory(logName);
            if (dir == null) {
                return List.of();
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return List.of();
            }
            for (File f : files) {
                LocalDate d = parseDatePrefix(f.getName());
                if (d != null) {
                    dates.add(d);
                }
            }
            return new ArrayList<>(dates);
        }

        File dataFolder = LottaLogs.getInstance().getDataFolder();

        File current = new File(dataFolder, LOGS_DIR);
        File compressed = new File(dataFolder, COMPRESSED_LOGS_DIR);

        collectDatesForLog(current, logName, dates);
        collectDatesForLog(compressed, logName, dates);

        return new ArrayList<>(dates);
    }

    private static void collectDatesForLog(File dir, String logName, TreeSet<LocalDate> dates) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String name = f.getName();
            if (name.length() < 11) continue;
            LocalDate d = parseDatePrefix(name);
            if (d == null) continue;
            String middle = name.substring(11);
            String stripped;
            if (middle.endsWith(GZ_EXT)) {
                stripped = middle.substring(0, middle.length() - GZ_EXT.length());
            } else if (middle.endsWith(LOG_EXT)) {
                stripped = middle.substring(0, middle.length() - LOG_EXT.length());
            } else {
                continue;
            }
            if (stripped.equals(logName)) {
                dates.add(d);
            }
        }
    }

    private static LocalDate parseDatePrefix(String fileName) {
        if (fileName.length() < 10) return null;
        String prefix = fileName.substring(0, 10);
        try {
            return LocalDate.parse(prefix);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private File additionalLogDirectory(String logName) {
        for (String entry : LottaLogs.getInstance().getConfig().getStringList("AdditionalLogs")) {
            int nameIdx = entry.indexOf("Name:");
            int pathIdx = entry.indexOf("Path:");
            if (nameIdx < 0 || pathIdx < 0) continue;
            String namePart = entry.substring(nameIdx + 5);
            int sp = namePart.indexOf(" ");
            if (sp < 0) continue;
            String name = namePart.substring(0, sp);
            if (!name.equals(logName)) continue;
            String path = entry.substring(pathIdx + 5);
            return new File(path);
        }
        return null;
    }

    @Override
    public Stream<String> streamLogLines(String logName, LocalDate date) throws IOException {
        Objects.requireNonNull(logName, "logName must not be null");
        Objects.requireNonNull(date, "date must not be null");

        Path path = resolveLogPath(logName, date);
        if (path == null) {
            throw new IOException("No log file found for " + logName + " on " + date);
        }

        String fileName = path.getFileName().toString();
        if (fileName.endsWith(GZ_EXT)) {
            FileInputStream fis = new FileInputStream(path.toFile());
            try {
                GZIPInputStream gzis = new GZIPInputStream(fis);
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8));
                return reader.lines().onClose(() -> closeQuietly(reader));
            } catch (IOException e) {
                closeQuietly(fis);
                throw e;
            }
        }

        return Files.lines(path, StandardCharsets.UTF_8);
    }

    private Path resolveLogPath(String logName, LocalDate date) {
        boolean isAdditional = Logging.getAdditionalLogNames().contains(logName);
        String dateStr = date.toString();

        if (isAdditional) {
            File dir = additionalLogDirectory(logName);
            if (dir == null) return null;
            File[] files = dir.listFiles();
            if (files == null) return null;
            for (File f : files) {
                if (f.getName().startsWith(dateStr)) {
                    return f.toPath();
                }
            }
            return null;
        }

        File dataFolder = LottaLogs.getInstance().getDataFolder();
        File current = new File(dataFolder, LOGS_DIR + File.separator + dateStr + "-" + logName + LOG_EXT);
        if (current.isFile()) {
            return current.toPath();
        }
        File compressed = new File(dataFolder, COMPRESSED_LOGS_DIR + File.separator + dateStr + "-" + logName + GZ_EXT);
        if (compressed.isFile()) {
            return compressed.toPath();
        }
        return null;
    }

    private static void closeQuietly(AutoCloseable c) {
        try { c.close(); } catch (Exception ignored) {}
    }

    @Override
    public SearchResult searchLogs(SearchQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        String logName = query.getLogName();
        SearchMode mode = query.getMode();

        if (mode == SearchMode.ADDITIONAL && !Logging.getAdditionalLogNames().contains(logName)) {
            return new SearchResult(logName, 0, List.of(), false);
        }

        Pattern compiled = null;
        if (query.getPattern() != null && query.isRegex()) {
            compiled = Pattern.compile(query.getPattern(), Pattern.CASE_INSENSITIVE);
        }
        String substring = (!query.isRegex() && query.getPattern() != null)
                ? query.getPattern().toLowerCase()
                : null;

        List<String> argKeyOrder = Collections.emptyList();
        if (mode == SearchMode.SPECIAL) {
            Log log = Logging.getCachedLogs().get(logName);
            if (log != null) {
                argKeyOrder = new ArrayList<>(log.getArguments().keySet());
            }
        }

        List<LocalDate> dates = filterDates(query, getAvailableDates(logName));

        List<LogEntry> entries = new ArrayList<>();
        boolean truncated = false;

        outer:
        for (LocalDate date : dates) {
            try (Stream<String> stream = streamLogLines(logName, date)) {
                for (String line : (Iterable<String>) stream::iterator) {
                    if (entries.size() >= query.getMaxResults()) {
                        truncated = true;
                        break outer;
                    }

                    if (substring != null && !line.toLowerCase().contains(substring)) {
                        continue;
                    }
                    if (compiled != null && !compiled.matcher(line).find()) {
                        continue;
                    }

                    Map<String, String> parsed = (mode == SearchMode.SPECIAL)
                            ? parseSpecialLine(line, argKeyOrder)
                            : Collections.emptyMap();

                    if (mode == SearchMode.SPECIAL) {
                        if (parsed.isEmpty() && (!query.getArgFilters().isEmpty() || query.getCenterLocation() != null)) {
                            continue;
                        }
                        if (!matchesArgFilters(parsed, query.getArgFilters())) {
                            continue;
                        }
                        if (!matchesLocation(parsed, query.getCenterLocation(), query.getRadius())) {
                            continue;
                        }
                    }

                    entries.add(new LogEntry(date, line, parsed));
                }
            } catch (IOException e) {
                LottaLogs.getInstance().getLogger().warning(
                        "Failed to read log file for " + logName + " on " + date + ": " + e.getMessage()
                );
            }
        }

        return new SearchResult(logName, entries.size(), entries, truncated);
    }

    private static List<LocalDate> filterDates(SearchQuery q, List<LocalDate> available) {
        if (available.isEmpty()) return List.of();

        LocalDate from = q.getDateFrom();
        LocalDate to = q.getDateTo();

        if (from != null && to != null) {
            LocalDate lo = from.isAfter(to) ? to : from;
            LocalDate hi = from.isAfter(to) ? from : to;
            return available.stream()
                    .filter(d -> !d.isBefore(lo) && !d.isAfter(hi))
                    .collect(Collectors.toList());
        }

        int pastDays = q.getPastDays();
        if (pastDays < 0) {
            return new ArrayList<>(available);
        }
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.minusDays(pastDays);
        return available.stream()
                .filter(d -> !d.isBefore(cutoff) && !d.isAfter(today))
                .collect(Collectors.toList());
    }

    private static Map<String, String> parseSpecialLine(String line, List<String> argKeyOrder) {
        if (!line.startsWith("|") || !line.endsWith("|")) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        String cursor = line;
        while (cursor.indexOf('|') + 1 != cursor.length() && cursor.contains(":")) {
            try {
                cursor = cursor.substring(cursor.indexOf('|') + 1);
                int colon = cursor.indexOf(':');
                if (colon < 0) break;
                String key = cursor.substring(0, colon);
                cursor = cursor.substring(colon + 1);
                int pipe = cursor.indexOf('|');
                if (pipe < 0) break;
                String value = cursor.substring(0, pipe);
                cursor = cursor.substring(pipe);
                result.put(key, value);
            } catch (RuntimeException e) {
                break;
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!argKeyOrder.isEmpty()) {
            Map<String, String> ordered = new LinkedHashMap<>();
            for (String key : argKeyOrder) {
                if (result.containsKey(key)) {
                    ordered.put(key, result.get(key));
                }
            }
            for (Map.Entry<String, String> e : result.entrySet()) {
                ordered.putIfAbsent(e.getKey(), e.getValue());
            }
            return ordered;
        }
        return result;
    }

    private static boolean matchesArgFilters(Map<String, String> parsed, Map<String, String> filters) {
        if (filters.isEmpty()) return true;
        for (Map.Entry<String, String> e : filters.entrySet()) {
            String actual = parsed.get(e.getKey());
            if (actual == null) return false;
            if (!actual.toLowerCase().contains(e.getValue().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesLocation(Map<String, String> parsed, Location center, int radius) {
        if (center == null || radius <= 0) return true;
        String locStr = parsed.get("Location");
        if (locStr == null) return false;
        Location parsedLoc = parseBetterLocation(locStr);
        if (parsedLoc == null) return false;
        if (parsedLoc.getWorld() == null || center.getWorld() == null) return false;
        if (!parsedLoc.getWorld().equals(center.getWorld())) return false;
        double dx = parsedLoc.getX() - center.getX();
        double dz = parsedLoc.getZ() - center.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= radius;
    }

    private static Location parseBetterLocation(String value) {
        try {
            String s = value;
            int worldIdx = s.indexOf("World:");
            if (worldIdx < 0) return null;
            s = s.substring(worldIdx + 6).trim();
            int sp = s.indexOf(' ');
            if (sp < 0) return null;
            String worldName = s.substring(0, sp);
            int xIdx = s.indexOf("X:");
            int yIdx = s.indexOf("Y:");
            int zIdx = s.indexOf("Z:");
            if (xIdx < 0 || yIdx < 0 || zIdx < 0) return null;
            double x = Double.parseDouble(s.substring(xIdx + 2, yIdx).trim());
            double y = Double.parseDouble(s.substring(yIdx + 2, zIdx).trim());
            double z = Double.parseDouble(s.substring(zIdx + 2).trim());
            return new Location(org.bukkit.Bukkit.getWorld(worldName), x, y, z);
        } catch (RuntimeException e) {
            return null;
        }
    }

}
