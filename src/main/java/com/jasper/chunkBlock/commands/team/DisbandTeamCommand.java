package com.jasper.chunkBlock.commands.team;

import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.gui.chunk.DisbandChunkGUI;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DisbandTeamCommand extends SubCommand {
    private TeamService teamService;


    public DisbandTeamCommand(String name, String description, String syntax, TeamService teamService) {
        super(name, description, syntax);
        this.teamService = teamService;
    }

    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Deletes a team";
    }

    @Override
    public String getSyntax() {
        return "/c disband <teamname>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length > 1) {
            String teamName = args[1];
            Team team = teamService.getTeamByPlayer(player.getUniqueId());

            if (team == null) {
                MessageUtils.sendError(player, "&f" + teamName + "&7 does not exist.");
                return;
            } else {
                if (team.getOwner().equals(player.getUniqueId())) {
                    DisbandChunkGUI disbandTeamGUI = new DisbandChunkGUI(player,teamService,teamService.getChunkByPlayer(player.getUniqueId()));
                    disbandTeamGUI.open();
                }
                else if (!team.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not the owner of this team");
                } else {
                    player.sendMessage(ChatColor.RED + "Team does not exist.");
                }
            }


        }
    }
}
