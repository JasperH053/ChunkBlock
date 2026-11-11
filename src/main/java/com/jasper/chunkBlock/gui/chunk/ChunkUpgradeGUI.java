package com.jasper.chunkBlock.gui.chunk;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.chunk.levels.LevelConfig;
import com.jasper.chunkBlock.chunk.levels.LevelStorage;
import com.jasper.chunkBlock.chunk.settings.Setting;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.util.MessageUtils;
import com.mojang.brigadier.Message;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

public class ChunkUpgradeGUI {

    private final Player player;
    private final Team team;
    private int levelCount = 1;
    private ClaimedChunk claimedChunk;
    LevelStorage levelStorage = ChunkBlock.getInstance().getLevelStorage();
    Economy econ = ChunkBlock.getEconomy();

    private static final int TOTAL_LEVELS = 50;

    public ChunkUpgradeGUI(Player player, Team team, ClaimedChunk claimedChunk) {
        this.player = player;
        this.team = team;
        this.claimedChunk = claimedChunk;
    }

    public void open(Player player, Team team) {
        int currentLevel = claimedChunk.getLevel();

        ChestGui gui = new ChestGui(6, ChatColor.DARK_GRAY + "Chunk - Upgrade");
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        StaticPane levelPane = new StaticPane(9, 5);
        StaticPane blueFiller = new StaticPane(9, 6);

        int[][] blueFillerPattern = {
                {6},
                {0, 1, 2, 3, 4, 6},
                {6},
                {1, 2, 3, 4, 5, 6},
                {6},
                {1, 2, 3, 5, 6, 7, 8}
        };

        int[][] snakePattern = {
                {0, 1, 2, 3, 4, 5},
                {5},
                {5, 4, 3, 2, 1, 0},
                {0},
                {0, 1, 2, 3, 4, 5}
        };

        for (int y = 0; y < snakePattern.length; y++) {
            int[] rowPositions = snakePattern[y];

            for (int posX : rowPositions) {
                if (levelCount > TOTAL_LEVELS) break;

                LevelConfig config = levelStorage.getLevelConfig(levelCount);

                Material color = levelCount <= currentLevel ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                ItemStack item = new ItemStack(color);
                ItemMeta meta = item.getItemMeta();
                final int targetLevel = levelCount;

                meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Level: " + levelCount);

                if (config != null) {
                    meta.setLore(config.toLore());
                } else {
                    meta.setLore(Collections.singletonList(ChatColor.RED + "No configuration."));
                }
                item.setItemMeta(meta);

                GuiItem guiItem = new GuiItem(item, event -> {
                    event.setCancelled(true);

                    if (targetLevel <= currentLevel) {
                        player.sendMessage(ChatColor.RED + "Your team has already unlocked this level!");
                        return;
                    }
                    if (targetLevel > currentLevel + 1) {
                        player.sendMessage(ChatColor.RED + "You first need to unlock level " + (currentLevel + 1));
                        return;
                    }
                    if (config == null) {
                        player.sendMessage(ChatColor.RED + "No config found for this level.");
                        return;
                    }

                    int cost = config.getCost();

                    if (!econ.has(player, cost)) {
                        player.sendMessage(ChatColor.RED + "You need: $" + cost + " for that");
                        return;
                    }

                    // Succes
                    econ.withdrawPlayer(player, cost);
                    ChunkBlock.getInstance().getDatabase().saveChunkUpgradeAsync(claimedChunk);

                    WorldBorder oldBorder = claimedChunk.getBorder();
                    claimedChunk.setLevel(targetLevel);
                    claimedChunk.setBorderSize(config.getChunkSize());

                    claimedChunk.resizeRegion();
                    for (UUID p : team.getMembersOfTeam()) {
                        Player member = Bukkit.getPlayer(p);
                        if (oldBorder != null) {
                            claimedChunk.applyBorder(member);
                            MessageUtils.sendSuccess(member, "Successfully upgraded to level: " + targetLevel + "!");
                        }
                    }

                    new ChunkUpgradeGUI(player, team, claimedChunk).open(player, team);
                });
                levelPane.addItem(guiItem, Slot.fromXY(posX, y));
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
                blueFiller.addItem(guiItem, Slot.fromXY(posX, y));
            }
        }

        StaticPane progressPane = new StaticPane(9, 1);

        ItemStack progressItem = new ItemStack(Material.BOOK);
        ItemMeta progressMeta = progressItem.getItemMeta();
        progressMeta.setDisplayName(ChatColor.WHITE + "Level -> " + ChatColor.GRAY + currentLevel);

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
        progressPane.addItem(progressGuiItem, Slot.fromXY(4, 0));

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        meta.setDisplayName("§cBack");
        barrier.setItemMeta(meta);

        progressPane.addItem(new GuiItem(barrier, event -> {
            ChunkMainGUI ch = new ChunkMainGUI(player, team);
            ch.open();
        }), Slot.fromXY(0, 0));

        gui.addPane(Slot.fromXY(0, 0), blueFiller);   // Background
        gui.addPane(Slot.fromXY(0, 0), levelPane);    // Levels
        gui.addPane(Slot.fromXY(0, 5), progressPane); // Progressbar

        gui.show(player);
    }
}