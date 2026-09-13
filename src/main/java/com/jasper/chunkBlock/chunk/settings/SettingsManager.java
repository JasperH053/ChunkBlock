package com.jasper.chunkBlock.chunk.settings;

import com.jasper.chunkBlock.chunk.levels.LevelConfig;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsManager {

    private final File configFile;
    private FileConfiguration config;
    HashMap<String, Setting> availableSettings = new HashMap<>();

    public SettingsManager(JavaPlugin plugin) {
        this.configFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!configFile.exists()) {
            plugin.saveResource("settings.yml", false); // kopieer uit resources
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void loadFromConfig() {
        org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("flags");

        if (section == null) { return; }

        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".name");
            String material = section.getString(key + ".material");

            Setting newSetting = new Setting(key, displayName, material);
            availableSettings.put(key, newSetting);
        }
    }

    public HashMap<String, Setting> getAvailableSettings() {
        return availableSettings;
    }
}
