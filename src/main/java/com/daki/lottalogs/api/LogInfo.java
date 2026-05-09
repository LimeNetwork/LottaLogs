package com.daki.lottalogs.api;

import java.util.List;
import java.util.Objects;

public record LogInfo(
        String name,
        boolean enabled,
        int daysOfLogsToKeep,
        List<String> blacklistedStrings,
        List<String> arguments
) {

    public LogInfo {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(blacklistedStrings, "blacklistedStrings must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        blacklistedStrings = List.copyOf(blacklistedStrings);
        arguments = List.copyOf(arguments);
    }

}
