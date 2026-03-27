package com.daki.lottalogs.other;

import com.daki.lottalogs.LottaLogs;
import lombok.Getter;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class APIs {

    @Getter
    private static final HashMap<String, Boolean> foundAPIsForLogs = new HashMap<>();

    @Getter
    private static boolean farmLimiterFound;
    @Getter
    private static GriefPrevention griefPreventionAPI;

    public static void connectAPIs() {

        connectFarmLimiter();
        sendFoundMessage("FarmLimiter", farmLimiterFound);
        foundAPIsForLogs.put("FarmLimiterLog", farmLimiterFound);

        connectGriefPrevention();
        sendFoundMessage("GriefPrevention", griefPreventionAPI != null);
        foundAPIsForLogs.put("GriefPreventionClaimsCreatedLog", griefPreventionAPI != null);
        foundAPIsForLogs.put("GriefPreventionClaimsDeletedLog", griefPreventionAPI != null);
        foundAPIsForLogs.put("GriefPreventionClaimsExpiredLog", griefPreventionAPI != null);
        foundAPIsForLogs.put("GriefPreventionClaimsResizedLog", griefPreventionAPI != null);
    }

    private static void connectFarmLimiter() {
        try {

            farmLimiterFound = (Bukkit.getServer().getPluginManager().getPlugin("FarmLimiter") != null);

        } catch (Throwable ignored) {
        }
    }

    private static void connectGriefPrevention() {
        try {

            griefPreventionAPI = (GriefPrevention) Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");

        } catch (Throwable ignored) {
        }
    }

    private static void sendFoundMessage(String pluginName, boolean found) {

        if (found) {
            LottaLogs.getInstance().getLogger().info(ConsoleColors.BLUE_BRIGHT + pluginName + " found!" + ConsoleColors.RESET);
        } else {
            LottaLogs.getInstance().getLogger().info(ConsoleColors.RED + pluginName + " not found!" + ConsoleColors.RESET);
        }

    }

}
