package com.jasper.chunkBlock.commands.chunk;

import com.jasper.chunkBlock.chunk.ChunkStorage;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ChunkSetHomeCommand extends SubCommand {

    private TeamService teamService;

    public ChunkSetHomeCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public void perform(Player player, String[] args) {
        Team team = teamService.getTeamByPlayer(player.getUniqueId());
        ClaimedChunk claimedChunk = teamService.getChunkByPlayer(player.getUniqueId());

        claimedChunk.setHome(player.getLocation());
        try {
            claimedChunk.saveHomeToDb();
            MessageUtils.sendSuccess(player, "Succesfully set new home!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    public String getDescription() {
        return "Sets a chunk home";
    }

    @Override
    public String getSyntax() {
        return "/c sethome";
    }
}
