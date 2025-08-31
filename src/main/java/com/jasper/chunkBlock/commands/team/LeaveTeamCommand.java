package com.jasper.chunkBlock.commands.team;

import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveTeamCommand extends SubCommand {

    private TeamService teamService;

    public LeaveTeamCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length > 1) {
            String teamName = args[1];
            Team team = teamService.getTeamByName(teamName); // Always fetch the team by name

            if (team == null) {
                MessageUtils.sendError(player, "Team does not exist!");
                return;
            }

            if (!team.getMembersOfTeam().contains(player.getUniqueId())) {
                MessageUtils.sendError(player, "You are not a member of this team!");
                return;
            }

            if (team.getOwner().equals(player.getUniqueId())) {
                MessageUtils.sendError(player, "You are the owner of this team, /c disband <team>");
                return;
            }

            teamService.removeMember(teamName, player.getUniqueId());

            player.sendMessage(ChatColor.GREEN + "You have left the team " + teamName + ".");
        } else {
            MessageUtils.sendInfo(player, "Please pecify a teamname!");
        }
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leaves a team";
    }

    @Override
    public String getSyntax() {
        return "/c leave";
    }

}
