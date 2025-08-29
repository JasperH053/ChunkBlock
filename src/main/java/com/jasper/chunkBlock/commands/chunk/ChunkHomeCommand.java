package com.jasper.chunkBlock.commands.chunk;

import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ChunkHomeCommand extends SubCommand {

    private TeamService teamService;

    public ChunkHomeCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getDescription() {
        return "Teleports to chunk home";
    }

    @Override
    public String getSyntax() {
        return "/c home";
    }

    @Override
    public void perform(Player player, String[] args) {
        Team team = teamService.getTeamByPlayer(player.getUniqueId());
        ClaimedChunk claimedChunk = teamService.getChunkByPlayer(player.getUniqueId());
        if (team == null || claimedChunk == null) {
            MessageUtils.sendError(player, "You are not in a Team!");
            return;
        } else {
            Location home = claimedChunk.getHome();
            if (home == null) {
                Bukkit.getLogger().info("Could not find home for player: " + player.getName());
                return;
            } else {
                player.teleport(home);
            }
        }

    }
}
