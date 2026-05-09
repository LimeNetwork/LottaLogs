package com.daki.lottalogs.api;

import java.util.List;

public record LogInfo(
        String name,
        boolean enabled,
        int daysOfLogsToKeep,
        List<String> blacklistedStrings,
        List<String> arguments
) {

    public LogInfo {
        blacklistedStrings = List.copyOf(blacklistedStrings);
        arguments = List.copyOf(arguments);
    }

}
