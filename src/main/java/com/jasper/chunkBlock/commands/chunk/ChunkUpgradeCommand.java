package com.jasper.chunkBlock.commands.chunk;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.gui.chunk.ChunkUpgradeGUI;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.entity.Player;

public class ChunkUpgradeCommand extends SubCommand {

    private final TeamService teamService;

    public ChunkUpgradeCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public void perform(Player player, String[] args) {
        Team team = teamService.getTeamByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtils.sendError(player, "You are not in a Team!");
            return;
        }

        ClaimedChunk claimedChunk = teamService.getChunkByPlayer(player.getUniqueId());
        if (claimedChunk == null) {
            MessageUtils.sendError(player, "You are not in a Chunk!");
            return;
        }

        ChunkUpgradeGUI chunkUpgradeGUI = new ChunkUpgradeGUI(player, team, claimedChunk);
        chunkUpgradeGUI.open(player, team);
    }

    @Override
    public String getName() {
        return "upgrade";
    }

    @Override
    public String getDescription() {
        return "Upgrade your chunk to a higher level";
    }

    @Override
    public String getSyntax() {
        return "/c upgrade";
    }
}
