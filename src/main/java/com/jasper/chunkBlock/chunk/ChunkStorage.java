package com.jasper.chunkBlock.chunk;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.levels.LevelConfig;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.util.MessageUtils;
import com.mojang.brigadier.Message;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import com.sk89q.worldguard.domains.DefaultDomain;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ChunkStorage {

    private final Map<String, ClaimedChunk> chunksByTeamId = new HashMap<>();
    private Database database = ChunkBlock.getInstance().getDatabase();
    FileConfiguration config = ChunkBlock.getInstance().getConfig();

    public ClaimedChunk createChunk(Team team, String chunkId, World world, Player player) {
        int level = 1;

        org.bukkit.Chunk bukkitChunk = player.getLocation().getChunk();

        ClaimedChunk claimedChunk = new ClaimedChunk(
                chunkId,
                team.getTeamId(),
                team.getOwner().toString(),
                level,
                world.getName(),
                (int) player.getX(),
                (int) player.getY(), // using player location at first creation for home.
                (int) player.getZ(),
                bukkitChunk.getX(),
                bukkitChunk.getZ(),
                config.getInt("defaultChunkSize")
        );
        claimedChunk.setHome(player.getLocation());
        claimedChunk.applyBorder(player);

        chunksByTeamId.put(team.getTeamId(), claimedChunk);
        database.addChunk(claimedChunk);
        createWorldGuardRegion(claimedChunk, team, world);

        return claimedChunk;
    }

    public void createWorldGuardRegion(ClaimedChunk chunk, Team team, World world) {
        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));

        if (manager == null) return;

        String regionId = "team_" + team.getTeamId();

        int minX = chunk.getX() * 16;
        int minZ = chunk.getZ() * 16;

        int maxX = minX + 15;
        int maxZ = minZ + 15;

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);

        ProtectedRegion region = new ProtectedCuboidRegion(regionId, min, max);
        region.setFlag(Flags.EXIT, StateFlag.State.DENY);
        region.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL);
        region.setFlag(Flags.EXIT_VIA_TELEPORT, StateFlag.State.ALLOW);
        region.setFlag(Flags.EXIT_DENY_MESSAGE, "§cYou can't walk here.");

        manager.addRegion(region);

        DefaultDomain owners = region.getOwners();
        owners.addPlayer(team.getOwner());

        DefaultDomain members = region.getMembers();
        for (UUID memberUuid : team.getMembersOfTeam()) {
            members.addPlayer(memberUuid);
        };

        manager.addRegion(region);
    }

    public void deleteChunk(Team team) {
        ClaimedChunk claimedChunk = getChunkByTeamId(team.getTeamId());

        if (claimedChunk == null) return;

        World world = claimedChunk.getWorld();
        if (world != null) {
            RegionManager manager = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .get(BukkitAdapter.adapt(world));

            if (manager != null) {
                manager.removeRegion("team_" + team.getTeamId());
            }
        }

        chunksByTeamId.remove(team.getTeamId());
    }

    public void addClaimedChunk(String teamId, ClaimedChunk claimedChunk) {
        chunksByTeamId.put(teamId,claimedChunk);
    }

    public ClaimedChunk getChunkByTeamId(String teamId) {
        return chunksByTeamId.get(teamId);
    }



}
