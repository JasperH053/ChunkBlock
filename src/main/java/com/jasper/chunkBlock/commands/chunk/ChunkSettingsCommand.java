package com.jasper.chunkBlock.commands.chunk;

import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.gui.chunk.ChunkSettingsGUI;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import org.bukkit.entity.Player;


public class ChunkSettingsCommand extends SubCommand {


    private ChunkSettingsGUI chunkSettingsGUI;
    private final TeamService teamService;
    private final SettingsManager settingsManager;

    public ChunkSettingsCommand(String name, String description, String syntax, TeamService teamService, SettingsManager settingsManager) {
        super(name, description, syntax);
        this.teamService = teamService;
        this.settingsManager = settingsManager;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (teamService.isPlayerInAnyTeam(player.getUniqueId())) {
            Team team = teamService.getTeamByPlayer(player.getUniqueId());
            ClaimedChunk claimedChunk = teamService.getClaimedChunkByTeamId(team.getTeamId());
            this.chunkSettingsGUI = new ChunkSettingsGUI(player, team, claimedChunk, settingsManager);
            this.chunkSettingsGUI.open();
        } else {
            player.sendMessage("You are not in a team");
        }
    }


    @Override
    public String getName() {
        return "settings";
    }
    @Override
    public String getDescription() {
        return "Opens your chunk settings";
    }
    @Override
    public String getSyntax() {
        return "/c settings";
    }
}
