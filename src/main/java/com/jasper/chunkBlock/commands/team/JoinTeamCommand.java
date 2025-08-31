package com.jasper.chunkBlock.commands.team;

import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinTeamCommand extends SubCommand {

    private final TeamService teamService;

    public JoinTeamCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Joins a team";
    }

    @Override
    public String getSyntax() {
        return "/c join <teamname>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length > 1) {
            String teamName = args[1];
            Team targetTeam = teamService.getTeamByName(teamName);

            if (!teamService.isPlayerInAnyTeam(player.getUniqueId())) {
                if (targetTeam != null) {
                    ClaimedChunk claimedChunk = teamService.getClaimedChunkByTeamId(targetTeam.getTeamId());

                    teamService.addMember(targetTeam.getTeamId(), player);
                    teamService.applyBorderForPlayer(player, claimedChunk);
                } else {
                    MessageUtils.sendError(player,  "Team " + teamName +" does not exist!");
                }
            } else {
                MessageUtils.sendError(player, "You are already in a team!");
            }
        }
    }
}
