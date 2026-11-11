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
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.RegisteredServiceProvider;


public final class ChunkBlock extends JavaPlugin {

    private static ChunkBlock instance;

    private File settings;
    private FileConfiguration settingsConfig;
    private File levelsConfig;
    private FileConfiguration levels;

    // default config
    private FileConfiguration config;

    private Database database;
    private ChunkStorage chunkStorage;
    private TeamService teamService;
    private LevelStorage levelStorage;
    private SettingsManager settingsManager;

    // vault
    private static Economy econ = null;

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
        getCommand("c").setExecutor(new CommandManager(this, teamService, settingsManager));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this,teamService,database), this);

        // Vault Economy
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getLogger().info("[ChunkBlock] Plugin enabled");
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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public FileConfiguration getSettingsConfig() {
        return this.settingsConfig;
    }
    public static Economy getEconomy() {
        return econ;
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
