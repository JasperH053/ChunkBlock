package com.jasper.chunkBlock.commands;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.commands.chunk.ChunkHomeCommand;
import com.jasper.chunkBlock.commands.chunk.ChunkMainCommand;
import com.jasper.chunkBlock.commands.chunk.ChunkSetHomeCommand;
import com.jasper.chunkBlock.commands.chunk.ChunkUpgradeCommand;
import com.jasper.chunkBlock.commands.team.*;
import com.jasper.chunkBlock.commands.util.HulpCommand;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subcommands = new ArrayList<>();
    private ChunkBlock plugin;
    private TeamService teamService;

    public CommandManager(ChunkBlock plugin, TeamService teamService) {
        this.plugin = plugin;
        this.teamService = teamService;
        subcommands.add(new HulpCommand("", "", "", this));
        subcommands.add(new CreateTeamCommand("", "", "", plugin, "", teamService));//        subcommands.add(new LeaveTeamCommand("","","",team, teamStorage,borderStorage));
        subcommands.add(new JoinTeamCommand("","","",teamService));
        subcommands.add(new DisbandTeamCommand("","","",teamService));
        subcommands.add(new ChunkHomeCommand("","","", teamService));
        subcommands.add(new ChunkSetHomeCommand("","","",teamService));
//        subcommands.add(new ChunkSettingsCommand("","","",team,teamStorage));
//        subcommands.add(new ShowTeams("","","",teamStorage));
        subcommands.add(new ChunkUpgradeCommand("", "", "",teamService));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        Team team = teamService.getTeamByPlayer(p.getUniqueId());

        if (label.equalsIgnoreCase("c")) {
            if (args.length == 0) {
                if (team != null) {
                    ChunkMainCommand c = new ChunkMainCommand();
                    c.open(p, teamService);
                } else {
                    showHelp(p);
                }
                return true;
            } else {
                for (SubCommand sub : this.getSubCommands()) {
                    if (args[0].equalsIgnoreCase(sub.getName())) {
                        sub.perform(p, args);
                        return true;
                    }
                }
                showHelp(p);
                return true;
            }
        }
        return true;
    }


    public void showHelp(Player p) {
        p.sendMessage(ChatColor.BLUE + "ChunkBlock");

        for (int i = 0; i < getSubCommands().size(); i++) {
            p.sendMessage(ChatColor.AQUA + subcommands.get(i).getSyntax() + ChatColor.GRAY +  " -> " + getSubCommands().get(i).getDescription());
        }
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subcommands;
    }


}
