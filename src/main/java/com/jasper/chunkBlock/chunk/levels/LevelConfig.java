package com.jasper.chunkBlock.chunk.levels;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelConfig {
    private final int level;
    private final int chunkSize;
    private final Map<Material, Integer> rewards;
    private final List<String> unlocks;
    private final int cost;

    public LevelConfig(int level, int chunkSize, Map<Material, Integer> rewards, List<String> unlocks, int cost) {
        this.level = level;
        this.chunkSize = chunkSize;
        this.rewards = rewards;
        this.unlocks = unlocks;
        this.cost = cost;
    }

    public List<String> toLore() {
        List<String> lore = new ArrayList<>();

        // Basic Info
        lore.add(ChatColor.GRAY + "Chunk Size: " + ChatColor.WHITE + chunkSize + "x" + chunkSize);
        lore.add(ChatColor.GRAY + "Upgrade Cost: " + ChatColor.GREEN + "$" + cost);
        lore.add("");

        // Rewards
        if (rewards != null && !rewards.isEmpty()) {
            lore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "Rewards:");
            for (Map.Entry<Material, Integer> entry : rewards.entrySet()) {
                String name = entry.getKey().name().toLowerCase().replace("_", " ");
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

                lore.add(ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + name + ": " + ChatColor.WHITE + entry.getValue());
            }
            lore.add("");
        }

        // Unlocks
        if (unlocks != null && !unlocks.isEmpty()) {
            lore.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Unlocks:");
            for (String unlock : unlocks) {
                lore.add(ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + unlock);
            }
        }

        return lore;
    }

    // getters
    public int getLevel() {
        return level;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getCost() {
        return cost;
    }

    public List<String> getUnlocks() {
        return unlocks;
    }

    public Map<Material, Integer> getMaxBlocks() {
        return rewards;
    }
}