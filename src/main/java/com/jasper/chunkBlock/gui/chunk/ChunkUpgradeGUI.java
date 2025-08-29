package com.jasper.chunkBlock.gui.chunk;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.*;
import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.jasper.chunkBlock.chunk.levels.LevelConfig;
import com.jasper.chunkBlock.chunk.levels.LevelStorage;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Collections;


public class ChunkUpgradeGUI {

    private final Player player;
    private final Team team;
    FileConfiguration config = ChunkBlock.getInstance().getConfig();
    private ClaimedChunk claimedChunk;
    LevelStorage levelStorage = ChunkBlock.getInstance().getLevelStorage();


    private static final int TOTAL_LEVELS = 50;

    public ChunkUpgradeGUI(Player player, Team team, ClaimedChunk claimedChunk ) {
        this.player = player;
        this.team = team;
        this.claimedChunk = claimedChunk;
    }

    public void open(Player player, Team team) {
        int currentLevel = claimedChunk.getLevel();
        Bukkit.getLogger().info("level: " + currentLevel);

        ChestGui gui = new ChestGui(6, ChatColor.DARK_GRAY + "Chunk - Upgrade");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        StaticPane levelPane = new StaticPane(0, 0, 9, 5);
        StaticPane blueFiller = new StaticPane(0, 0, 9, 6);

        int[][] blueFillerPattern = {
                {6},
                {0,1,2,3,4,6},
                {6},
                {1,2,3,4,5,6},
                {6},
                {1,2,3,5,6,7,8}
        };

        int[][] snakePattern = {
                {0, 1, 2, 3, 4, 5},
                {5},
                {5, 4, 3, 2, 1, 0},
                {0},
                {0, 1, 2, 3, 4, 5}
        };

        int levelCount = 1;

        for (int y = 0; y < snakePattern.length; y++) {
            int[] rowPositions = snakePattern[y];

            for (int posX : rowPositions) {
                if (levelCount > TOTAL_LEVELS) break;
                LevelConfig config = levelStorage.getLevelConfig(levelCount);

                Material color = levelCount <= currentLevel ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                ItemStack item = new ItemStack(color);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "Level " + levelCount);
                if (config != null) {
                    meta.setLore(config.toLore());
                } else {
                    meta.setLore(Collections.singletonList(ChatColor.RED + "Geen configuratie gevonden."));
                }
                item.setItemMeta(meta);

                GuiItem guiItem = new GuiItem(item);
                levelPane.addItem(guiItem, posX, y);

                levelCount++;
            }
        }

        for (int y = 0; y < blueFillerPattern.length; y++) {
            int[] rowPositions = blueFillerPattern[y];
            for (int posX : rowPositions) {
                Material color = Material.BLUE_STAINED_GLASS_PANE;
                ItemStack item = new ItemStack(color);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("");
                item.setItemMeta(meta);

                GuiItem guiItem = new GuiItem(item);
                blueFiller.addItem(guiItem, posX, y);
            }
        }


        // Progress bar pane
        StaticPane progressPane = new StaticPane(0, 5, 9, 1);

        ItemStack progressItem = new ItemStack(Material.BOOK);
        ItemMeta progressMeta = progressItem.getItemMeta();
        progressMeta.setDisplayName(ChatColor.WHITE + "Level -> " + ChatColor.GRAY +  currentLevel);

        int percentage = (int) (((double) currentLevel / TOTAL_LEVELS) * 100);
        StringBuilder bar = new StringBuilder();

        bar.append(ChatColor.GRAY).append("[");

        for (int i = 1; i <= 20; i++) {
            if (i <= (percentage / 5)) {
                bar.append(ChatColor.GREEN).append("|");
            } else {
                bar.append(ChatColor.RED).append("|");
            }
        }

        bar.append(ChatColor.GRAY).append("]");

        progressMeta.setLore(Collections.singletonList(bar.toString() + ChatColor.GRAY + " (" + percentage + "%)"));
        progressItem.setItemMeta(progressMeta);

        GuiItem progressGuiItem = new GuiItem(progressItem);
        progressPane.addItem(progressGuiItem, 4, 0);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        meta.setDisplayName("§cBack");
        barrier.setItemMeta(meta);

        progressPane.addItem(new GuiItem(barrier, event -> {
            ChunkMainGUI ch = new ChunkMainGUI(player, team);
            ch.open();
        }), 0, 0);

        gui.addPane(blueFiller);  // eerst achtergrond
        gui.addPane(levelPane);   // dan levels erbovenop
        gui.addPane(progressPane);// progressbar helemaal bovenaan

        gui.show(player);
    }

    public int getTeamLevel() {
        return team.getLevel();
    }


}
