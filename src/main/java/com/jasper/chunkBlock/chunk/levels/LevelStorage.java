package com.jasper.chunkBlock.chunk.levels;


import com.jasper.chunkBlock.ChunkBlock;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelStorage {

    private final File configFile;
    private FileConfiguration config;
    private Map<Integer, LevelConfig> levelConfigs = new HashMap<>();

    public LevelStorage(JavaPlugin plugin) {
        this.configFile = new File(plugin.getDataFolder(), "levels.yml");
        if (!configFile.exists()) {
            plugin.saveResource("levels.yml", false); // Copy from resources
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveLevelsToConfig(Map<Integer, LevelConfig> levels) {
        File file = new File(ChunkBlock.getInstance().getDataFolder(), "levels.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (Map.Entry<Integer, LevelConfig> entry : levels.entrySet()) {
            int level = entry.getKey();
            LevelConfig levelConfig = entry.getValue();

            String path = "levels." + level;
            config.set(path + ".chunk-size", levelConfig.getChunkSize());

            Map<String, Integer> maxBlocksStringKeyed = new HashMap<>();
            for (Map.Entry<Material, Integer> blockEntry : levelConfig.getMaxBlocks().entrySet()) {
                maxBlocksStringKeyed.put(blockEntry.getKey().name(), blockEntry.getValue());
            }

            config.set(path + ".rewards", maxBlocksStringKeyed);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLevelConfig(int level, LevelConfig config) {
        levelConfigs.put(level, config);
        saveLevelsToConfig(levelConfigs);
    }

    public LevelConfig getLevelConfig(int level) {
        String path = "levels." + level;

        if (!config.contains(path)) {
            System.out.println("For level + " + level + ": PATH NOT AVAILABLE");
            return null;
        }

        int chunkSize = config.getInt(path + ".chunkSize");
        int cost = config.getInt(path + ".cost");

        Map<String, Object> rawMap = config.getConfigurationSection("levels." + level + ".rewards").getValues(false);
        Map<Material, Integer> rewards = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            Material material = Material.matchMaterial(entry.getKey());
            if (material != null && entry.getValue() instanceof Integer) {
                rewards.put(material, (Integer) entry.getValue());
            }
        }

        // Unlocks
        List<String> unlocks = config.getStringList(path + ".unlocks");

        return new LevelConfig(level, chunkSize, rewards, unlocks, cost);
    }

    public Map<Integer, LevelConfig> getLevelConfigs() {
        return levelConfigs;
    }
}
