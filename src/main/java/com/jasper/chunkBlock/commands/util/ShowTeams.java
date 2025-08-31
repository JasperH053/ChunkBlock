package com.jasper.chunkBlock.commands.util;

import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.entity.Player;

import java.util.Map;

public class ShowTeams extends SubCommand {

    TeamService teamService;

    public ShowTeams(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public void perform(Player player, String[] args) {
        int i = 0;
        for (Map.Entry<String, Team> entry : teamService.getTeams().entrySet()) {
            Team team = entry.getValue();

            MessageUtils.sendInfo(player, team.getTeamName() + ", &7" + team.getMembersOfTeam().size());
            i++;
        }
        MessageUtils.sendSuccess(player, "Total amount of teams: &7" + i);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Displays a list of all the teams";
    }

    @Override
    public String getSyntax() {
        return "/c list";
    }
}
