package com.jasper.chunkBlock.commands.border;

import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.gui.border.BorderSettingsGUI;
import com.jasper.chunkBlock.gui.chunk.ChunkSettingsGUI;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.entity.Player;

public class BorderSettingsCommand extends SubCommand {
    private BorderSettingsGUI borderSettingsGUI;
    private final TeamService teamService;

    public BorderSettingsCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (teamService.isPlayerInAnyTeam(player.getUniqueId())) {
            Team team = teamService.getTeamByPlayer(player.getUniqueId());
            ClaimedChunk claimedChunk = teamService.getClaimedChunkByTeamId(team.getTeamId());
            this.borderSettingsGUI = new BorderSettingsGUI(player, teamService, claimedChunk);
            this.borderSettingsGUI.open();
        } else {
            MessageUtils.sendError(player,"You are not in a team");
        }
    }

    @Override
    public String getName() {
        return "border";
    }

    @Override
    public String getDescription() {
        return "Opens the border settings GUI for the chunk.";
    }

    @Override
    public String getSyntax() {
        return "/c border>";
    }
}
