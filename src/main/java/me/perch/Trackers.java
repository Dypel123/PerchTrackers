package me.perch;

import me.perch.listeners.TrackerListener;
import me.perch.listeners.ConvertOldTrackersListener;
import me.perch.manager.ConfigManager;
import me.perch.manager.TrackerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Trackers extends JavaPlugin {

    private static Trackers instance;
    private TrackerManager trackerManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.trackerManager = new TrackerManager(this);
        this.trackerManager.loadTrackers();

        TrackerCommand trackerCmd = new TrackerCommand(this);
        getCommand("trackers").setExecutor(trackerCmd);
        getCommand("trackers").setTabCompleter(trackerCmd);

        getServer().getPluginManager().registerEvents(new TrackerListener(this), this);

        getServer().getPluginManager().registerEvents(new ConvertOldTrackersListener(this), this);

        getLogger().info("PerchTrackers enabled and listeners registered!");
    }

    public static Trackers getInstance() {
        return instance;
    }

    public TrackerManager getTrackerManager() {
        return trackerManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}