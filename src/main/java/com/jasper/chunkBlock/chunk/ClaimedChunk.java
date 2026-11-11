package com.jasper.chunkBlock.chunk;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.settings.Setting;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClaimedChunk {

    private final ChunkBlock plugin = ChunkBlock.getInstance();

    private final String teamId;
    private final String chunkId;
    private final String world;
    private final int x;
    private final int z;

    private int borderSize;
    private WorldBorder worldBorder;
    private Location home;
    private String owner;
    private int level;
    private double xp;

    private final Map<String, Object> upgrades = new HashMap<>();
    private final Map<String, Boolean> settings = new HashMap<>();

    public ClaimedChunk(String chunkid, String teamid, String ownerUuid, int level, String world,
                        int homeX, int homeY, int homeZ, int chunkX, int chunkZ, int borderSize) {
        this.chunkId = chunkid;
        this.teamId = teamid;
        this.owner = ownerUuid;
        this.level = level;
        this.world = world;
        this.x = chunkX;
        this.z = chunkZ;
        this.borderSize = borderSize;
        this.xp = 0.0;

        World w = Bukkit.getWorld(world);
        if (w != null) {
            this.home = new Location(w, homeX, homeY, homeZ);
        }

        for (Setting setting : plugin.getSettingsManager().getAvailableSettings().values()) {
            settings.put(setting.getId(), setting.isDefaultValue());
        }
    }

    // Geometry

    public void resizeRegion() {
        World world = getWorld();
        ProtectedRegion old = getRegion();
        if (world == null || old == null) return;

        ProtectedCuboidRegion updated = new ProtectedCuboidRegion(
                old.getId(),
                BlockVector3.at(getMinBlockX(), world.getMinHeight(), getMinBlockZ()),
                BlockVector3.at(getMaxBlockX(), world.getMaxHeight() - 1, getMaxBlockZ())
        );
        updated.copyFrom(old); // Copy members, owners and flags.

        WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(world))
                .addRegion(updated);
    }

    public int getChunksPerSide() {
        return Math.max(1, borderSize / 16);
    }
    private int getMinChunkX() {
        return x - (getChunksPerSide() - 1) / 2;
    }
    private int getMinChunkZ() {
        return z - (getChunksPerSide() - 1) / 2;
    }
    public int getMinBlockX() { return getMinChunkX() * 16; }
    public int getMinBlockZ() { return getMinChunkZ() * 16; }
    public int getMaxBlockX() { return getMinBlockX() + getChunksPerSide() * 16 - 1; }
    public int getMaxBlockZ() { return getMinBlockZ() + getChunksPerSide() * 16 - 1; }

    // Locatie & border

    public Location getCenter() {
        World w = Bukkit.getWorld(this.world);
        if (w == null) {
            throw new IllegalStateException("World not found! : " + this.world);
        }

        double centerX = (this.x * 16) + 8.0;
        double centerZ = (this.z * 16) + 8.0;

        // Hoogste blok opzoeken zodat spelers niet in de grond spawnen
        double y = w.getHighestBlockYAt((int) centerX, (int) centerZ) + 1.0;

        return new Location(w, centerX, y, centerZ);
    }

    public enum BorderColor { BLUE, GREEN, RED }

    public WorldBorder applyBorder(Player player) {
        return applyBorder(player, BorderColor.GREEN);
    }

    public WorldBorder applyBorder(Player player, BorderColor color) {
        double size = getChunksPerSide() * 16.0;
        double centerX = getMinBlockX() + size / 2.0;
        double centerZ = getMinBlockZ() + size / 2.0;

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(centerX, centerZ);

        long eenJaar = 60L * 60 * 24 * 365; // seconden
        double verschil = 0.01;

        switch (color) {
            case GREEN -> {
                border.setSize(size);
                border.changeSize(size + verschil, eenJaar);
            }
            case RED -> {
                border.setSize(size + verschil);
                border.changeSize(size, eenJaar);
            }
            default -> border.setSize(size);
        }

        player.setWorldBorder(border);
        this.worldBorder = border;

        return border;
    }

    public void removeBorder(Player player) {
        player.setWorldBorder(null);
        this.worldBorder = null;
    }

    public WorldBorder getBorder() {
        return worldBorder;
    }

    public ProtectedRegion getRegion() {
        World bukkitWorld = Bukkit.getWorld(this.world);
        if (bukkitWorld == null) return null;

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(bukkitWorld));

        if (manager == null) return null;

        return manager.getRegion("team_" + this.teamId);
    }

    // Database

    public void loadHomeFromDb() throws SQLException {
        String sql = "SELECT home_x, home_y, home_z FROM chunks WHERE chunkid = ?";
        try (Connection con = plugin.getDatabase().getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, this.chunkId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    plugin.getLogger().warning("No chunk record found for chunkid=" + this.chunkId);
                    this.home = null;
                    return;
                }

                if (rs.getObject("home_x") == null) {
                    plugin.getLogger().info("Home yet to be set for chunkid=" + this.chunkId);
                    this.home = null;
                    return;
                }

                double hx = rs.getDouble("home_x");
                double hy = rs.getDouble("home_y");
                double hz = rs.getDouble("home_z");

                World w = Bukkit.getWorld(this.world);
                if (w == null) {
                    plugin.getLogger().warning("World not loaded: " + this.world
                            + " (chunkId=" + this.chunkId + "). Home will be null.");
                    this.home = null;
                    return;
                }

                this.home = new Location(w, hx, hy, hz);
                plugin.getLogger().info("Home geladen voor chunkId=" + this.chunkId
                        + " @ " + hx + "," + hy + "," + hz + " in wereld " + this.world);
            }
        }
    }

    public void saveHomeToDb() throws SQLException {
        String sql = "UPDATE chunks SET home_x = ?, home_y = ?, home_z = ? WHERE chunkid = ?";
        try (Connection con = plugin.getDatabase().getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (this.home == null) {
                ps.setNull(1, java.sql.Types.DOUBLE);
                ps.setNull(2, java.sql.Types.DOUBLE);
                ps.setNull(3, java.sql.Types.DOUBLE);
            } else {
                if (home.getWorld() == null || !home.getWorld().getName().equals(this.world)) {
                    throw new IllegalArgumentException("Home world not the same as chunk's world: " + this.world);
                }
                ps.setDouble(1, home.getX());
                ps.setDouble(2, home.getY());
                ps.setDouble(3, home.getZ());
            }

            ps.setString(4, this.chunkId);
            ps.executeUpdate();
        }
    }

    // Settings

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

    public Map<String, Boolean> getSettings() {
        return settings;
    }

    // Upgrades, level & xp

    public void setUpgrade(String key, Object value) { this.upgrades.put(key, value); }
    public Object getUpgrade(String key) { return this.upgrades.get(key); }
    public boolean hasUpgrade(String key) { return this.upgrades.containsKey(key); }
    public Map<String, Object> getUpgrades() { return upgrades; }

    public int getLevel() { return level; }
    public void setLevel(int newLevel) { this.level = newLevel; }

    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    public void addXp(double amount) { this.xp += amount; }

    // Getters / setters

    public String getChunkId() { return chunkId; }
    public String getTeamId() { return teamId; }
    public String getWorldName() { return world; }
    public World getWorld() { return Bukkit.getWorld(this.world); }
    public int getX() { return x; }
    public int getZ() { return z; }

    public int getBorderSize() { return borderSize; }
    public void setBorderSize(int borderSize) { this.borderSize = borderSize; }

    public Location getHome() { return home; }
    public void setHome(Location location) { this.home = location; }

    public UUID getOwner() {
        return owner == null ? null : UUID.fromString(owner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaimedChunk other)) return false;
        return x == other.x && z == other.z && world.equals(other.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}