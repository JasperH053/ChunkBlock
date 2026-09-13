package com.jasper.chunkBlock.database;

import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class Database {

    private final String url;

    public Database(String path) throws SQLException {
        this.url = "jdbc:sqlite:" + path;

        // Init schema op een verse connection
        try (Connection con = DriverManager.getConnection(url);
             Statement statement = con.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA busy_timeout = 5000");

            statement.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    teamid TEXT PRIMARY KEY,
                    owner TEXT,
                    teamname TEXT NOT NULL
                );
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS team_members (
                    teamid TEXT NOT NULL,
                    member_uuid TEXT NOT NULL,
                    PRIMARY KEY (teamid, member_uuid)
                );
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS chunks (
                    chunkid TEXT PRIMARY KEY,
                    teamid TEXT NOT NULL,
                    owner_uuid TEXT,
                    level INT,
                    levelxp DOUBLE,
                    world TEXT,
                    home_x INTEGER,
                    home_y INTEGER,
                    home_z INTEGER,
                    center_x INTEGER,
                    center_z INTEGER,
                    border_radius INTEGER
                );
            """);
        }
    }

    /** Geef altijd een verse, open Connection terug met de juiste PRAGMAs. */
    public Connection getConnectionF() throws SQLException {
        Connection con = DriverManager.getConnection(url);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = 5000");
        }
        return con;
    }

    /** Niets te sluiten bij per-call connections; method blijft voor backwards compat. */
    public void closeConnection() { /* no-op */ }

    public void addChunk(ClaimedChunk claimedChunk) {
        String sql = """
            INSERT INTO chunks (chunkid, teamid, owner_uuid, level, levelxp, world,
                                home_x, home_y, home_z, center_x, center_z, border_radius)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, claimedChunk.getChunkId());
            ps.setString(2, claimedChunk.getTeamId());
            ps.setString(3, claimedChunk.getOwner().toString());
            ps.setInt(4, claimedChunk.getLevel());
            ps.setDouble(5, claimedChunk.getXp());
            ps.setString(6, claimedChunk.getWorldName());

            if (claimedChunk.getHome() == null) {
                ps.setNull(7, Types.INTEGER);
                ps.setNull(8, Types.INTEGER);
                ps.setNull(9, Types.INTEGER);
            } else {
                ps.setInt(7, claimedChunk.getHome().getBlockX());
                ps.setInt(8, claimedChunk.getHome().getBlockY());
                ps.setInt(9, claimedChunk.getHome().getBlockZ());
            }

            ps.setInt(10, claimedChunk.getX());
            ps.setInt(11, claimedChunk.getZ());
            ps.setInt(12, claimedChunk.getClaimRadius());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ClaimedChunk getChunkByOwner(UUID ownerUuid) {
        String sql = "SELECT * FROM chunks WHERE owner_uuid = ?";
        try (Connection con = getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new ClaimedChunk(
                        rs.getString("chunkid"),
                        rs.getString("teamid"),
                        rs.getString("owner_uuid"),
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
            return null;
        }
    }

    public void addTeam(Team team) {
        String sql = "INSERT INTO teams (teamid, owner, teamname) VALUES (?, ?, ?)";
        try (Connection con = getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, team.getTeamId());
            ps.setString(2, team.getOwner().toString());
            ps.setString(3, team.getTeamName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteTeam(String teamId) {
        // Optioneel: transactioneel uitvoeren
        try (Connection con = getConnectionF()) {
            con.setAutoCommit(false);
            try (PreparedStatement psMembers = con.prepareStatement(
                    "DELETE FROM team_members WHERE teamid = ?")) {
                psMembers.setString(1, teamId);
                psMembers.executeUpdate();
            }
            try (PreparedStatement psChunk = con.prepareStatement(
                    "DELETE FROM chunks WHERE teamid = ?")) {
                psChunk.setString(1, teamId);
                psChunk.executeUpdate();
            }
            int affectedTeam;
            try (PreparedStatement psTeam = con.prepareStatement(
                    "DELETE FROM teams WHERE teamid = ?")) {
                psTeam.setString(1, teamId);
                affectedTeam = psTeam.executeUpdate();
            }
            con.commit();
            return affectedTeam > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addMember(String teamId, Player player) {
        String sql = "INSERT INTO team_members (teamid, member_uuid) VALUES (?, ?)";
        try (Connection con = getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, teamId);
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeMember(Player player) {
        String sql = "DELETE FROM team_members WHERE member_uuid = ?";
        try (Connection con = getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, player.getUniqueId().toString());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean teamIdExists(String teamId) {
        String sql = "SELECT 1 FROM teams WHERE teamid = ?";
        try (Connection con = getConnectionF();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateChunkLevel(ClaimedChunk chunk, int newLevel) {
        String sql = "UPDATE chunks SET level = ? WHERE world = ? AND center_x = ? AND center_z = ?";
        try (Connection con = getConnectionF();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newLevel);
            ps.setString(2, chunk.getWorldName());
            ps.setInt(3, chunk.getX());
            ps.setInt(4, chunk.getZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateChunkLevelAsync(ClaimedChunk chunk, int newLevel) {
        Bukkit.getScheduler().runTaskAsynchronously(ChunkBlock.getInstance(),
                () -> updateChunkLevel(chunk, newLevel));
    }
}
