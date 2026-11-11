package com.jasper.chunkBlock.commands.chunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkLevel {

    private int level;
    private int xp;

    private static final int MAX_LEVEL = 10;

    public ChunkLevel(int level, int xp) {
        this.level = level;
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public void addXp(int amount) {
        this.xp += amount;

        while (xp >= getXpForNextLevel() && level < MAX_LEVEL) {
            xp -= getXpForNextLevel();
            level++;
        }
    }

    public int getXpForNextLevel() {
        return 100 + (level * 50);
    }

    public boolean isMaxLevel() {
        return level >= MAX_LEVEL;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("level", level);
        data.put("xp", xp);
        return data;
    }

    public static ChunkLevel deserialize(Map<String, Object> data) {
        int level = (int) data.get("level");
        int xp = (int) data.get("xp");
        return new ChunkLevel(level, xp);
    }
}
