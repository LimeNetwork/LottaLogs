package com.daki.lottalogs.api.search;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;

public final class SearchQuery {

    private final String logName;
    private final SearchMode mode;
    private final int pastDays;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final String pattern;
    private final boolean regex;
    private final Map<String, String> argFilters;
    private final Location centerLocation;
    private final int radius;
    private final int maxResults;

    private SearchQuery(Builder b) {
        this.logName = b.logName;
        this.mode = b.mode;
        this.pastDays = b.pastDays;
        this.dateFrom = b.dateFrom;
        this.dateTo = b.dateTo;
        this.pattern = b.pattern;
        this.regex = b.regex;
        this.argFilters = Map.copyOf(b.argFilters);
        this.centerLocation = b.centerLocation;
        this.radius = b.radius;
        this.maxResults = b.maxResults;
    }

    public String getLogName() { return logName; }
    public SearchMode getMode() { return mode; }
    public int getPastDays() { return pastDays; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public String getPattern() { return pattern; }
    public boolean isRegex() { return regex; }
    public Map<String, String> getArgFilters() { return argFilters; }
    public Location getCenterLocation() { return centerLocation; }
    public int getRadius() { return radius; }
    public int getMaxResults() { return maxResults; }

    public static Builder builder(String logName) {
        return new Builder(logName);
    }

    public static final class Builder {

        private String logName;
        private SearchMode mode = SearchMode.NORMAL;
        private int pastDays = -1;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String pattern;
        private boolean regex = false;
        private Map<String, String> argFilters = new HashMap<>();
        private Location centerLocation;
        private int radius = 0;
        private int maxResults = Integer.MAX_VALUE;

        private Builder(String logName) {
            this.logName = logName;
        }

        public Builder logName(String logName) { this.logName = logName; return this; }
        public Builder mode(SearchMode mode) { this.mode = mode; return this; }
        public Builder pastDays(int pastDays) { this.pastDays = pastDays; return this; }
        public Builder dateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; return this; }
        public Builder dateTo(LocalDate dateTo) { this.dateTo = dateTo; return this; }
        public Builder pattern(String pattern) { this.pattern = pattern; return this; }
        public Builder regex(boolean regex) { this.regex = regex; return this; }
        public Builder argFilters(Map<String, String> argFilters) {
            if (argFilters == null) {
                this.argFilters = new HashMap<>();
                return this;
            }
            Map<String, String> copy = new HashMap<>(argFilters.size());
            for (Map.Entry<String, String> e : argFilters.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) {
                    throw new IllegalArgumentException("argFilters must not contain null keys or values");
                }
                copy.put(e.getKey(), e.getValue());
            }
            this.argFilters = copy;
            return this;
        }
        public Builder addArgFilter(String key, String value) {
            Objects.requireNonNull(key, "argFilter key must not be null");
            Objects.requireNonNull(value, "argFilter value must not be null");
            this.argFilters.put(key, value);
            return this;
        }
        public Builder centerLocation(Location centerLocation) { this.centerLocation = centerLocation; return this; }
        public Builder radius(int radius) { this.radius = radius; return this; }
        public Builder maxResults(int maxResults) { this.maxResults = maxResults; return this; }

        public SearchQuery build() {
            Objects.requireNonNull(logName, "logName must not be null");
            if (mode == null) {
                throw new IllegalStateException("mode must not be null");
            }
            if (maxResults <= 0) {
                throw new IllegalArgumentException("maxResults must be > 0 (got " + maxResults + ")");
            }
            if (radius < 0) {
                throw new IllegalArgumentException("radius must be >= 0 (got " + radius + ")");
            }
            if (pastDays < -1) {
                throw new IllegalArgumentException("pastDays must be -1 (unbounded) or >= 0 (got " + pastDays + ")");
            }
            return new SearchQuery(this);
        }

    }

}
