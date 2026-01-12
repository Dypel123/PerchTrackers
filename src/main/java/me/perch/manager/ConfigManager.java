package me.perch.manager;

import me.perch.Trackers;
import me.perch.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Trackers plugin;

    public ConfigManager(Trackers plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getMessage(String path) {
        return ColorUtil.colorize(plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path));
    }

    public String getPrefix() {
        return getMessage("prefix");
    }
}