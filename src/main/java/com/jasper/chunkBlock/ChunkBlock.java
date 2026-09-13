package com.jasper.chunkBlock;

import com.jasper.chunkBlock.chunk.ChunkStorage;
import com.jasper.chunkBlock.chunk.levels.LevelConfig;
import com.jasper.chunkBlock.chunk.levels.LevelStorage;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.commands.CommandManager;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.listeners.PlayerJoinListener;
import com.jasper.chunkBlock.team.TeamService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChunkBlock extends JavaPlugin {

    private static ChunkBlock instance;

    private File settings;
    private FileConfiguration settingsConfig;
    private File levelsConfig;
    private FileConfiguration levels;

    // default
    private FileConfiguration config;

    private Database database;
    private ChunkStorage chunkStorage;
    private TeamService teamService;
    private LevelStorage levelStorage;
    private SettingsManager settingsManager;


    @Override
    public void onEnable() {
        instance = this;

        // Config inladen
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(configFile);

        createLevelsConfig();
        createSettingsConfig();

        // Database connectie opzetten
        try {
            database = new Database(getDataFolder().getAbsolutePath() + "/chunkblock.db");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
            Bukkit.getLogger().severe("FEADSDSADSADSAKODMSADSKOASDN");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Services initialiseren
        this.chunkStorage = new ChunkStorage();
        this.teamService = new TeamService(database, this.chunkStorage);
        this.levelStorage = new LevelStorage(this);
        this.settingsManager = new SettingsManager(this);

        teamService.loadAllTeams();
        settingsManager.loadFromConfig();

        // Commands & events registreren
        getCommand("c").setExecutor(new CommandManager(this, teamService));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this,teamService,database), this);

        Bukkit.getLogger().info("[ChunkBlock] Plugin enabled ✔");
        Bukkit.getLogger().info("[ChunkBlock] Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.closeConnection();
        }
        instance = null;
        Bukkit.getLogger().info("[ChunkBlock] Plugin disabled.");
    }

    public void createLevelsConfig() {
        levelsConfig = new File(getDataFolder(), "levels.yml");
        if (!levelsConfig.exists()) {
            levelsConfig.getParentFile().mkdirs();
            saveResource("levels.yml", false);
        }

        levels = new YamlConfiguration();
        try {
            levels.load(levelsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void createSettingsConfig() {
        settings = new File(getDataFolder(), "settings.yml");
        if (!settings.exists()) {
            settings.getParentFile().mkdirs();
            saveResource("settings.yml", false);
        }

        settingsConfig = new YamlConfiguration();
        try {
            settingsConfig.load(settings);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getSettingsConfig() {
        return this.settingsConfig;
    }

    public LevelStorage getLevelStorage() {
        return levelStorage;
    }

    public static ChunkBlock getInstance() {
        return instance;
    }

    public FileConfiguration getLevelsConfig() {
        return levels;
    }

    public Database getDatabase() {
        return database;
    }

    public TeamService getTeamService() {
        return teamService;
    }

    public SettingsManager getSettingsManager() { return settingsManager; }


    public ChunkStorage getChunkStorage() {
        return chunkStorage;
    }

}
