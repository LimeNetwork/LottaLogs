package com.daki.lottalogs;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import com.daki.lottalogs.api.LottaLogsAPI;
import com.daki.lottalogs.api.impl.LottaLogsAPIImpl;
import com.daki.lottalogs.other.APIs;
import com.daki.lottalogs.other.Config;
import com.daki.lottalogs.other.Logging;
import com.daki.lottalogs.other.Metrics;
import com.daki.lottalogs.other.Register;

public class LottaLogs extends JavaPlugin {

    @Getter
    private static LottaLogs instance;

    @Override
    public void onLoad() {

        instance = this;

    }

    @Override
    public void onEnable() {

        LottaLogs.getInstance().getLogger().info("--------------------------------------------------");
        LottaLogs.getInstance().getLogger().info("LottaLogs starting...");

        long start = System.currentTimeMillis();

        Config.checkAndReloadConfig();
        Config.configSetup();

        Logging.initiate();

        APIs.connectAPIs();

        Register.registerEvents();
        Register.registerCommands();

        new Metrics(this, 19063);

        Bukkit.getServicesManager().register(
                LottaLogsAPI.class,
                new LottaLogsAPIImpl(),
                this,
                ServicePriority.Normal
        );

        long end = System.currentTimeMillis();

        LottaLogs.getInstance().getLogger().info("LottaLogs loaded, took " + (end - start) + "ms.");
        LottaLogs.getInstance().getLogger().info("--------------------------------------------------");

    }

}
