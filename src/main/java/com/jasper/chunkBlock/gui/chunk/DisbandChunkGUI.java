package com.jasper.chunkBlock.gui.chunk;

import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.gui.base.ConfirmationGUI;

import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.entity.Player;

public class DisbandChunkGUI extends ConfirmationGUI {

    private Player player;
    private TeamService teamService;
    private ClaimedChunk claimedChunk;

    public DisbandChunkGUI(Player player, TeamService teamService, ClaimedChunk claimedChunk) {
        super(player, "Confirmation");
        this.teamService = teamService;
        this.player = player;
        this.claimedChunk = claimedChunk;
    }

    @Override
    protected void onConfirm() {
        Team team = teamService.getTeamByPlayer(player.getUniqueId());
        teamService.deleteTeam(team);
        MessageUtils.sendSuccess(player, "Chunk has succesfully been disbanded!");
    }

    @Override
    protected void onDeny() {
        // Do nothing, just close the inventory
    }

}

