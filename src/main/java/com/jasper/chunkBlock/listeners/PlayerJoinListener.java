package com.jasper.chunkBlock.listeners;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerJoinListener implements Listener {

    private final ChunkBlock plugin;
    private final TeamService teamService;
    private final Database database;

    public PlayerJoinListener(ChunkBlock plugin, TeamService teamService, Database database) {
        this.plugin = plugin;
        this.teamService = teamService;
        this.database = database;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!teamService.isPlayerInAnyTeam(player.getUniqueId())) {
                player.setWorldBorder(null);
                return;
            }

            Team team = teamService.getTeamByPlayer(player.getUniqueId());
            if (team == null) { player.setWorldBorder(null); return; }

            // Haal chunk via teamId, NIET via owner
            ClaimedChunk chunk = teamService.getClaimedChunkByTeamId(team.getTeamId());
            teamService.applyBorderForPlayer(player, chunk); // zorg dat deze null-safe is
        }, 1L);
    }

}
