package com.jasper.chunkBlock.chunk;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.levels.LevelConfig;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.team.Team;
import org.bukkit.Bukkit;
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

        ClaimedChunk claimedChunk = new ClaimedChunk(chunkId, team.getTeamId(), team.getOwner().toString(), level, world.getName(), (int) player.getX(), (int) player.getY(), (int) player.getZ(), (int) player.getX(), (int) player.getZ(), config.getInt("defaultChunkSize"));
        claimedChunk.setHome(player.getLocation());
        claimedChunk.createBorder(player);

        chunksByTeamId.put(team.getTeamId(), claimedChunk);
        database.addChunk(claimedChunk);

        return claimedChunk;
    }


    public void deleteChunk(Team team) {
        chunksByTeamId.remove(team.getTeamId());
    }

    public void addClaimedChunk(String teamId, ClaimedChunk claimedChunk) {
        chunksByTeamId.put(teamId,claimedChunk);
    }

    public ClaimedChunk getChunkByTeamId(String teamId) {
        return chunksByTeamId.get(teamId);
    }



}
