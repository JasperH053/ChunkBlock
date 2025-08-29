package com.jasper.chunkBlock.commands.team;

import com.jasper.chunkBlock.commands.SubCommand;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;

public class CreateTeamCommand extends SubCommand {

    private String teamName;
    private final JavaPlugin plugin;
    private UUID owner;
    private TeamService teamService;

    public CreateTeamCommand(String name, String description, String syntax, JavaPlugin plugin, String teamName, TeamService teamService) {
        super(name, description, syntax);
        this.plugin = plugin;
        this.teamName = teamName;
        this.teamService = teamService;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Creates a chunk team";
    }

    @Override
    public String getSyntax() {
        return "/c create";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length > 1) {
            teamName = args[1];
            owner = player.getUniqueId();

            if (teamService.getTeamByPlayer(owner) == null) {
                teamService.createTeam(teamName, player);
            } else {
                MessageUtils.sendError(player, "&cYou joined a team already!");
            }

        }
    }




}
