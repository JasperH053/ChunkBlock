package com.jasper.chunkBlock.team;

import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Team {

    private final String teamId;
    private String teamName;
    private final Set<UUID> members = new HashSet<>();
    private UUID owner;
    private int level;

    public Team(String teamId, UUID owner, String teamName) {
        this.teamId = teamId;
        this.owner = owner;
        this.teamName = teamName;
        this.members.add(owner);
    }

    public List<String> getMembersAsStringList() {
        return members.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
    }

    public void onJoin(Player player) {
        if (player == null) return;
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && !member.getUniqueId().equals(player.getUniqueId())) {
                MessageUtils.sendSuccess(player,"&fYou just joined " + teamName + "!");
                MessageUtils.sendInfo(member, "&f" + player.getName() +" &fJust joined party: " + teamName );
            }
        }
    }

    public void onLeave(Player player) {
        if (player == null) return;
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && !member.getUniqueId().equals(player.getUniqueId())) {
                MessageUtils.sendSuccess(player,"&fYou just left " + teamName + "!");
                MessageUtils.sendInfo(member, "&f" + player.getName() +" &fJust left party: " + teamName );
            }
        }
        Bukkit.getLogger().info(Arrays.toString(members.toArray()));
    }

    public void addMember(UUID member) {
        this.members.add(member);
    }

    public void removeMember(UUID member) {
        this.members.remove(member);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    public void setLevel(int newLevel) {
        level = newLevel;
    }
    public String getTeamId() {
        return teamId;
    }
    public int getLevel() {
        return level;
    }
    public String getTeamName() { return teamName; }
    public UUID getOwner() { return this.owner; }
    public Set<UUID> getMembersOfTeam() { return members; }

}
