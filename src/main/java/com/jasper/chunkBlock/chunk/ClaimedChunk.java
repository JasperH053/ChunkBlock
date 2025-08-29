package com.jasper.chunkBlock.chunk;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.settings.SettingType;
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
    private final Map<SettingType, Boolean> settings = new HashMap<>();

    public ClaimedChunk(String chunkid, String teamid, String ownerUuid, int level, String world, int homeX, int homeY, int homeZ, int centerX, int centerZ, int borderRadius) {
        this.world = world;
        this.radius = borderRadius;
        this.owner = ownerUuid;
        this.x = centerX;
        this.z = centerZ;
        this.teamId = teamid;
        this.chunkId = chunkid;
        this.xp = xp;
        this.level = level;
        for (SettingType type : SettingType.values()) {
            settings.put(type, false);
        }
    }

    public Location getCenter(ClaimedChunk claimedChunk) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            throw new IllegalStateException("World not found! : " + world);
        }

        // Y-waarde kan je zo laten of ook uit db halen als je dat wilt
        double y = 64;

        return new Location(w, x, y, z);
    }


    public WorldBorder createBorder(Player player) {
        WorldBorder border = Bukkit.createWorldBorder();
        Location location = player.getLocation();

        border.setCenter(location);
        border.setSize(level * radius * 2); // 32 blokken per chunk, *2 voor diameter

        player.setWorldBorder(border);
        return border;
    }

    public WorldBorder removeBorder() {
        return worldBorder = null;
    }


    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }
    public String getTeamId() { return teamId; }
    public int getLevel() { return level; }
    public double getXp() { return xp; }
    public boolean isSettingEnabled(SettingType type) { return settings.getOrDefault(type, false); }
    public Map<String, Object> getUpgrades() { return upgrades; }
    public Map<SettingType, Boolean> getSettings() { return settings; }

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
    public void toggleSetting(SettingType type) {
        settings.put(type, !isSettingEnabled(type));
    }
    public void setSetting(SettingType type, boolean value) {
        settings.put(type, value);
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