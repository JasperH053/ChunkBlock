package com.jasper.chunkBlock.chunk;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.settings.Setting;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClaimedChunk {

    private final String teamId;
    private final String chunkId;
    private final String world;
    private WorldBorder worldBorder;
    private final double radius;
    private Location home;
    private String owner;
    private int homeX;
    private int homeY;
    private int homeZ;
    private final int x;
    private final int z;
    private int level;
    private double xp;
    private final Map<String, Object> upgrades = new HashMap<>();
    private final Map<String, Boolean> settings = new HashMap<>();

    public ClaimedChunk(String chunkid, String teamid, String ownerUuid, int level, String world, int homeX, int homeY, int homeZ, int centerX, int centerZ, int borderRadius) {
        this.world = world;
        this.radius = borderRadius;
        this.owner = ownerUuid;
        this.x = centerX;
        this.z = centerZ;
        this.teamId = teamid;
        this.chunkId = chunkid;
        this.xp = 0.0;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.level = level;
        for (Setting setting : ChunkBlock.getInstance().getSettingsManager().getAvailableSettings().values()) {
            settings.put(setting.getId(), setting.isDefaultValue());
        }
    }

    public Location getCenter() {
        World w = Bukkit.getWorld(this.world);
        if (w == null) {
            throw new IllegalStateException("World not found! : " + this.world);
        }

        // 1. Vermenigvuldig de chunk-coördinaat met 16 om bij het startblok te komen
        // 2. Tel er 8 bij op om exact in het midden (center) van de 16x16 chunk te staan
        double centerX = (this.x * 16) + 8.0;
        double centerZ = (this.z * 16) + 8.0;

        // Tip: In plaats van vast op y=64, kun je het hoogste blok opzoeken
        // zodat een speler bij /c sethome niet in de grond vast komt te zitten!
        double y = w.getHighestBlockYAt((int) centerX, (int) centerZ) + 1.0;

        return new Location(w, centerX, y, centerZ);
    }

    public WorldBorder createBorder(Player player) {
        WorldBorder border = Bukkit.createWorldBorder();

        // 1. Bereken de grote blok-coördinaten
        double centerBlockX = (this.x * 16) + 8.0;
        double centerBlockZ = (this.z * 16) + 8.0;

        // 2. Geef uitsluitend de zojuist berekende, grote getallen door
        border.setCenter(centerBlockX, centerBlockZ);

        // 3. Forceer de grootte op 16 (1 chunk)
        border.setSize(16.0);

        player.setWorldBorder(border);
        this.worldBorder = border;

        // Debug bericht ter verificatie
        player.sendMessage("§a[DEBUG] Border berekend op blokken: " + centerBlockX + ", " + centerBlockZ);

        return border;
    }


    public ProtectedRegion getRegion() {
        World bukkitWorld = Bukkit.getWorld(this.world); // Gebruik jouw eigen world String
        if (bukkitWorld == null) return null;

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(bukkitWorld));

        if (manager == null) return null;

        String regionId = "team_" + this.teamId; // Zorg dat this.teamId klopt met jouw variabelenaam
        return manager.getRegion(regionId);
    }

    public void removeBorder(Player player) {
        player.setWorldBorder(null);
        this.worldBorder = null;
    }

    // database
    public String getWorldName() {
        return this.world;
    }

    public org.bukkit.World getWorld() {
        return org.bukkit.Bukkit.getWorld(this.world);
    }
    public int getX() { return x; }
    public int getZ() { return z; }
    public String getTeamId() { return teamId; }
    public int getLevel() { return level; }
    public double getXp() { return xp; }
    public boolean isSettingEnabled(SettingsManager type) { return settings.getOrDefault(type, false); }
    public Map<String, Object> getUpgrades() { return upgrades; }
    public Map<String, Boolean> getSettings() { return settings; }

    public Location getHome() {
        return home;
    }

    public void loadHomeFromDb() throws SQLException {
        String sql = "SELECT home_x, home_y, home_z FROM chunks WHERE chunkid = ?";
        try (Connection con = ChunkBlock.getInstance().getDatabase().getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Belangrijk: bind de parameter
            ps.setString(1, String.valueOf(this.chunkId)); // of setInt/setLong afhankelijk van je type

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    Bukkit.getLogger().warning("[ChunkBlock] Geen chunk-record gevonden voor chunkId=" + this.chunkId);
                    this.home = null;
                    return;
                }

                if (rs.getObject("home_x") == null) {
                    Bukkit.getLogger().info("[ChunkBlock] Home is (nog) niet ingesteld voor chunkId=" + this.chunkId);
                    this.home = null;
                    return;
                }

                double x = rs.getDouble("home_x");
                double y = rs.getDouble("home_y");
                double z = rs.getDouble("home_z");

                World w = Bukkit.getWorld(this.world); // verwacht wereldnaam
                if (w == null) {
                    Bukkit.getLogger().warning("[ChunkBlock] Wereld niet geladen of onbekend: " + this.world
                            + " (chunkId=" + this.chunkId + "). Home blijft null.");
                    this.home = null;
                    return;
                }

                this.home = new Location(w, x, y, z);
                Bukkit.getLogger().info("[ChunkBlock] Home geladen voor chunkId=" + this.chunkId
                        + " @ " + x + "," + y + "," + z + " in wereld " + this.world);
            }
        }
    }


    public void saveHomeToDb() throws SQLException {
        String sql = "UPDATE chunks SET home_x = ?, home_y = ?, home_z = ? WHERE chunkid = ?";
        try (Connection con = ChunkBlock.getInstance().getDatabase().getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (this.home == null) {
                ps.setNull(1, java.sql.Types.DOUBLE);
                ps.setNull(2, java.sql.Types.DOUBLE);
                ps.setNull(3, java.sql.Types.DOUBLE);
            } else {
                if (home.getWorld() == null || !home.getWorld().getName().equals(this.world)) {
                    throw new IllegalArgumentException("Home-wereld komt niet overeen met chunk-wereld: " + this.world);
                }
                ps.setDouble(1, home.getX());
                ps.setDouble(2, home.getY());
                ps.setDouble(3, home.getZ());
            }

            ps.setString(4, this.chunkId);
            ps.executeUpdate();
        }
    }

    public String getChunkId() {
        return chunkId;
    }
    public void toggleSetting(Setting setting) {
        String id = setting.getId();
        boolean currentState = settings.getOrDefault(id, setting.isDefaultValue());
        settings.put(id, !currentState);
    }
    public boolean isSettingEnabled(String settingId) {
        return settings.getOrDefault(settingId, false);
    }

    public void setSetting(String settingId, boolean value) {
        settings.put(settingId, value);
    }


    public void setHome(Location location) {
        home = location;
    }
    public void setXp(double xp) { this.xp = xp; }
    public void addXp(double amount) { this.xp += amount; }
    public void setUpgrade(String key, Object value) { this.upgrades.put(key, value); }
    public Object getUpgrade(String key) { return this.upgrades.get(key); }
    public boolean hasUpgrade(String key) { return this.upgrades.containsKey(key); }
    public int getClaimRadius() {
        return (int) radius;
    }
    public UUID getOwner() {
        return UUID.fromString(owner);
    }

    public WorldBorder getBorder() {
        return worldBorder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaimedChunk)) return false;
        ClaimedChunk other = (ClaimedChunk) o;
        return x == other.x && z == other.z && world.equals(other.world);
    }

    public void setLevel(int newLevel) {
        this.level = newLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}