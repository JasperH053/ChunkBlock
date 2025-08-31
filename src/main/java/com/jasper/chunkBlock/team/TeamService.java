package com.jasper.chunkBlock.team;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ChunkStorage;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.security.SecureRandom;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamService {

    private final Database database;
    private final ChunkStorage chunkStorage;
    private Map<String, Team> teamsById = new HashMap<>();
    private Map<UUID, Team> teamsByPlayer = new HashMap<>();

    public TeamService(Database database, ChunkStorage chunkStorage) {
        this.database = database;
        this.chunkStorage = chunkStorage;
    }

    public Team createTeam(String name, Player player) {
        String teamId = IdGenerator.generateId();
        String chunkId = IdGenerator.generateId();

        Set<UUID> members = new HashSet<>();
        members.add(player.getUniqueId());

        Team team = new Team(teamId, player.getUniqueId(),name);
        World world = player.getWorld();

        teamsById.put(teamId, team);
        teamsByPlayer.put(player.getUniqueId(), team);
        addMember(teamId,player);

        database.addTeam(team);
        chunkStorage.createChunk(team, chunkId, world, player);

        return team;
    }

    public void loadAllTeams() {
        // Leegmaken om dubbele entries te voorkomen bij reload
        teamsById.clear();
        teamsByPlayer.clear();
        // (optioneel) chunkStorage ook legen als je die opnieuw opbouwt
        // chunkStorage.clear();

        String teamSql   = "SELECT teamid, owner, teamname FROM teams";
        String memberSql = "SELECT member_uuid FROM team_members WHERE teamid = ?";

        try (Connection con = database.getConnectionF();
             PreparedStatement teamStmt = con.prepareStatement(teamSql);
             PreparedStatement memberStmt = con.prepareStatement(memberSql);
             ResultSet rs = teamStmt.executeQuery()) {

            while (rs.next()) {
                String teamId = rs.getString("teamid");
                String name   = rs.getString("teamname");

                String ownerStr = rs.getString("owner");
                UUID ownerUuid = (ownerStr == null || ownerStr.isEmpty())
                        ? null
                        : UUID.fromString(ownerStr);

                // Maak team en registreer in-memory
                Team team = new Team(teamId, ownerUuid, name);
                teamsById.put(teamId, team);
                if (ownerUuid != null) {
                    teamsByPlayer.put(ownerUuid, team); // ⬅️ owner óók indexeren
                }

                // Chunk(s) laden
                ClaimedChunk claimedChunk = loadChunkByTeamId(teamId);
                if (claimedChunk != null) {
                    chunkStorage.addClaimedChunk(teamId, claimedChunk);
                    try {
                        claimedChunk.loadHomeFromDb();
                        Bukkit.getLogger().info("[ChunkBlock] Home geladen voor team " + teamId);
                    } catch (SQLException e) {
                        Bukkit.getLogger().warning("[ChunkBlock] Fout bij loadHomeFromDb voor team " + teamId + ": " + e.getMessage());
                    }
                } else {
                    Bukkit.getLogger().warning("[ChunkBlock] Geen chunk gevonden voor team " + teamId);
                }

                // Members voor dit team
                memberStmt.clearParameters();
                memberStmt.setString(1, teamId);
                try (ResultSet memberRs = memberStmt.executeQuery()) {
                    while (memberRs.next()) {
                        UUID memberId = UUID.fromString(memberRs.getString("member_uuid"));
                        team.addMember(memberId);
                        teamsByPlayer.put(memberId, team); // ⬅️ members indexeren
                    }
                }
            }

            Bukkit.getLogger().info("[ChunkBlock] Alle teams en leden geladen uit database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ClaimedChunk loadChunkByTeamId(String teamId) {
        String sql = "SELECT * FROM chunks WHERE teamid = ?";
        chunkStorage.addClaimedChunk(teamId, getClaimedChunkByTeamId(teamId));
        try (PreparedStatement stmt = database.getConnectionF().prepareStatement(sql)) {
            stmt.setString(1, teamId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ClaimedChunk(
                        rs.getString("chunkid"),
                        rs.getString("teamid"),
                        (rs.getString("owner_uuid")),
                        rs.getInt("level"),
                        rs.getString("world"),
                        rs.getInt("home_x"),
                        rs.getInt("home_y"),
                        rs.getInt("home_z"),
                        rs.getInt("center_x"),
                        rs.getInt("center_z"),
                        rs.getInt("border_radius")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClaimedChunk getClaimedChunkByTeamId(String teamId) {
        try (PreparedStatement stmt = database.getConnectionF().prepareStatement("SELECT * FROM chunks WHERE teamid = ?")) {
            stmt.setString(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String chunkid = rs.getString("chunkid");
                String owner = rs.getString("owner_uuid");
                int level = rs.getInt("level");
                String world = rs.getString("world");
                int homeX = rs.getInt("home_x");
                int homeY = rs.getInt("home_y");
                int homeZ = rs.getInt("home_z");
                int centerX = rs.getInt("center_x");
                int centerZ = rs.getInt("center_z");
                int radius = rs.getInt("border_radius");

                return new ClaimedChunk(chunkid, teamId, owner, level, world, homeX, homeY, homeZ, centerX, centerZ, radius); // constructor moet hiermee matchen
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void applyBorders(Team team) {
        ClaimedChunk claimedChunk = chunkStorage.getChunkByTeamId(team.getTeamId());
        if (claimedChunk == null) return;

        for (UUID uuid : team.getMembersOfTeam()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                applyBorderForPlayer(player,claimedChunk);
            }
        }
    }

    public boolean deleteTeam(Team team) {
        try {
            for (UUID uuid : team.getMembersOfTeam()) {
                Player player = Bukkit.getPlayer(uuid);
                player.setWorldBorder(null);
                teamsByPlayer.remove(uuid);
                team.getMembersOfTeam().remove(player);
            }

            database.deleteTeam(team.getTeamId());
            teamsById.remove(team.getTeamId());
            chunkStorage.deleteChunk(team);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public ClaimedChunk getChunkByPlayer(UUID playerUuid) {
        ClaimedChunk claimeddChunk = chunkStorage.getChunkByTeamId(getTeamByPlayer(playerUuid).getTeamId());
        return claimeddChunk;
    }

    public Team getTeamByName(String targetTeamName) {
        Team team = teamsById.values().stream()
                .filter(t -> t.getTeamName().equalsIgnoreCase(targetTeamName))
                .findFirst()
                .orElse(null);
        return team;
    }

    public synchronized boolean isPlayerInAnyTeam(UUID uuid) {
        Team team = getTeamByPlayer(uuid);

        if (team != null) {
            if (team.getMembersOfTeam().contains(uuid)) {
                return true;
            }
        } else {
            return false;
        }

        return false;
    }

    public void applyBorderForPlayer(Player player, ClaimedChunk chunk) {
        if (chunk == null) {
            player.setWorldBorder(null);
            MessageUtils.sendError(player, "Chunk does not exist!");
            return;
        }

        World world = Bukkit.getWorld(chunk.getWorld());

        if (world == null ) {
            MessageUtils.sendError(player, "World does not exist!");
            return;
        }

        int centerX = chunk.getX();
        int centerZ = chunk.getZ();
        int centerY = world.getHighestBlockYAt(centerX, centerZ);

        Location center = new Location(world, centerX, centerY, centerZ);

        player.sendMessage("§7[DEBUG] Border center op: " + centerX + ", " + centerZ + " in wereld '" + world.getName() + "'");
        player.sendMessage("§7[DEBUG] Spelerlocatie: " + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ());

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(center);

        int radius = chunk.getClaimRadius(); // bijv. 1
        int level = chunk.getLevel();        // bijv. 1
        double size = radius; // diameter in blokken

        player.sendMessage("§7[DEBUG] Border size (diameter): " + size);

        border.setSize(size);
        border.setWarningDistance(5);
        border.setDamageAmount(0.5);
        border.setDamageBuffer(1);

        player.setWorldBorder(border);

        player.sendMessage("§a[DEBUG] Border succesvol toegepast!");
    }


    public boolean addMember(String teamId, Player player) {
        Team team = getTeamById(teamId);
        if (team == null) return false;

        database.addMember(teamId,player);
        teamsByPlayer.put(player.getUniqueId(), team);
        team.addMember(player.getUniqueId());
        team.onJoin(player);

        ClaimedChunk claimedChunk = getClaimedChunkByTeamId(team.getTeamId());
        try {
            claimedChunk.loadHomeFromDb();
            Location home = claimedChunk.getHome();
            player.teleport(home);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean removeMember(String teamName, UUID member) {
        Team team = getTeamByName(teamName);
        Player player = Bukkit.getPlayer(member);

        if (team == null) return false;

        if (team.getOwner().equals(member)) {
            return false;
        } else {
            database.removeMember(player);
            player.setWorldBorder(null);
            team.removeMember(member);
            team.onLeave(player);
            teamsByPlayer.remove(player.getUniqueId(), team);
        }

        return true;
    }
//
//    /**
//     * Zoek het team waar deze speler in zit (owner of member).
//     */
//    public Team getTeamFromPlayer(UUID playerUUID) {
//        for (Team team : teamStorage.getTeams().values()) { // teams is je Map<String, Team>
//            if (team.getMembersOfTeam().contains(playerUUID)) {
//                return team;
//            }
//        }
//        return null;
//    }

    public void addTeam(Team team) {
        teamsById.put(team.getTeamId(), team);

        List<String> memberUUIDStrings = team.getMembersOfTeam().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
    }

//
//    public void removeTeam(Team team, Player player) {
//        teams.remove(team.getTeamName());
//        loadTeams();
//        player.sendMessage(ChatColor.GREEN + "Team " + team.getTeamName() + " succesfully deleted");
//    }
//
//    public void addMemberToTeam(Team team, UUID playerUUID) {
//        team.joinTeam(playerUUID,this);
//        List<String> memberUUIDs = team.getMembersAsStringList();
//
//        team.onJoin(Bukkit.getPlayer(playerUUID));
//    }
//
//
//    public void upgrade(Team team, int level) {
////        team.upgrade( level);
//        for (UUID p : team.getMembersOfTeam()) {
//            Player player = Bukkit.getPlayer(p);
//            MessageUtils.sendSuccess(player, "&7" + team.getTeamName() + ",&f has now upgraded to level: " + team.getLevel());
//        }
//    }
//

    public Map<String, Team> getTeams() {
        return teamsById;
    }

    public synchronized Team getTeamByPlayer(UUID uuid) {
        return teamsByPlayer.get(uuid);
    }

    public Team getTeamById(String teamId) {
        return teamsById.get(teamId);
    }

    public class IdGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    public static String generateId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}


}
