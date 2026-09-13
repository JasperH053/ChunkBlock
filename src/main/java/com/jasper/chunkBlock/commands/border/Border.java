package com.jasper.chunkBlock.commands.border;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.settings.Setting;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.util.*;

public class Border {

    private final int x;             // chunk-coördinaat X
    private final int z;             // chunk-coördinaat Z
    private final String worldName;  // wereldnaam
    private final Team owner;        // eigenaar van deze border
    private final double radius;     // radius in blokken
    private Location defaultHome;

    private final Map<String, Boolean> settings = new HashMap<>();


    private boolean allowPvP = false;
    private boolean allowBuild = true;

    public Border(int x, int z, String worldName, Team owner, double radius,Location defaultHome) {
        this.x = x;
        this.z = z;
        this.worldName = worldName;
        this.owner = owner;
        this.radius = radius;
        this.defaultHome = defaultHome;
        for (Setting setting : ChunkBlock.getInstance().getSettingsManager().getAvailableSettings().values()) {
            settings.put(setting.getId(), setting.isDefaultValue());
        }
    }


    //–– Getters ––//
    public Location getDefaultHome() {
        return defaultHome;
    }
    public int getX() {
        return x;
    }
    public int getZ() {
        return z;
    }
    public String getWorldName() {
        return worldName;
    }
    public Team getOwner() {
        return owner;
    }
    public double getRadius() {
        return radius;
    }

}
