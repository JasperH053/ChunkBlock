package com.jasper.chunkBlock.gui.chunk;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ChunkStorage;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.database.Database;
import com.jasper.chunkBlock.gui.border.BorderSettingsGUI;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.util.MessageUtils;
import com.jasper.chunkBlock.team.TeamService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChunkMainGUI {

    private Player player;
    private final Team team;
    private TeamService teamService;
    private final SettingsManager settingsManager = ChunkBlock.getInstance().getSettingsManager();

    public ChunkMainGUI(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    public void open() {
        this.teamService = ChunkBlock.getInstance().getTeamService();
        ClaimedChunk claimedChunk = teamService.getChunkByPlayer(player.getUniqueId());

        if (!teamService.isPlayerInAnyTeam(player.getUniqueId())) {
            MessageUtils.sendError(player, "You are not in a team! [1]");
            return;
        }

        if (claimedChunk == null) {
            MessageUtils.sendError(player, "§cChunk not found!");
            return;
        }

        ChestGui gui = new ChestGui(5, "Chunk - Menu");
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        PatternPane paneel = new PatternPane(9, 5, patroon);

        OutlinePane background = new OutlinePane(9, 5, Pane.Priority.LOWEST);
        background.addItem(new GuiItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE)));
        background.setRepeat(true);

        OutlinePane navigationPane = new OutlinePane(3, 3);
        OutlinePane circleCenter = new OutlinePane(1, 1, Pane.Priority.HIGH);

        // HOME BUTTON
        ItemStack shop = new ItemStack(Material.OAK_DOOR);
        ItemMeta shopMeta = shop.getItemMeta();
        shopMeta.setDisplayName("Chunk - Home");
        shop.setItemMeta(shopMeta);
        navigationPane.addItem(new GuiItem(shop, event -> {
            Location locationHome = claimedChunk.getHome();
            player.teleport(locationHome);
            player.closeInventory();
            MessageUtils.sendSuccess(player, "Teleported to your chunk home");
        }));

        // SETTINGS BUTTON
        ItemStack settings = new ItemStack(Material.BOOK);
        ItemMeta settingsMeta = settings.getItemMeta();
        settingsMeta.setDisplayName("Chunk - Settings");
        settings.setItemMeta(settingsMeta);
        navigationPane.addItem(new GuiItem(settings, event -> {
            ChunkSettingsGUI ch = new ChunkSettingsGUI(player, team, claimedChunk,settingsManager);
            ch.open();
        }));

        // BORDER BUTTON
        ItemStack border = new ItemStack(Material.BARRIER);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName("Chunk - Border ");
        border.setItemMeta(borderMeta);
        navigationPane.addItem(new GuiItem(border, event -> {
            BorderSettingsGUI borderSettingsGUI = new BorderSettingsGUI(player, teamService, claimedChunk);
            borderSettingsGUI.open();
        }));

        // CHUNK PLAYERS
        ItemStack players = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta  playerMeta = players.getItemMeta();
        playerMeta.setDisplayName("Chunk - Players");
        players.setItemMeta(playerMeta);

        navigationPane.addItem(new GuiItem(players, event -> {
            ChunkPlayersGUI chunkPlayersGUI = new ChunkPlayersGUI(player,team, claimedChunk);
            chunkPlayersGUI.open();
        }));

        // CHUNK TOP
        ItemStack chunkTop = new ItemStack(Material.ACACIA_HANGING_SIGN);
        ItemMeta  chunkTopMeta = chunkTop.getItemMeta();
        chunkTopMeta.setDisplayName("Chunk - Top");
        chunkTop.setItemMeta(chunkTopMeta);
        navigationPane.addItem(new GuiItem(chunkTop, event -> {
            ChunkTopGUI chunkTopGUI = new ChunkTopGUI(player,team,claimedChunk);
            chunkTopGUI.open();
        }));

        // CHUNK UPGRADE
        ItemStack upgrade = new ItemStack(Material.CHEST);
        ItemMeta  upgradeMeta = upgrade.getItemMeta();
        upgradeMeta.setDisplayName("Chunk - Upgrade");
        upgrade.setItemMeta(upgradeMeta);
        navigationPane.addItem(new GuiItem(upgrade, event -> {
            ChunkUpgradeGUI upgradeGUI = new ChunkUpgradeGUI(player,team, claimedChunk);
            upgradeGUI.open(player,team);
        }));

        // CHUNK CIRCLE STATS
        ItemStack chunk = new ItemStack(Material.BEACON);
        ItemMeta  chunkMeta = chunk.getItemMeta();
        chunkMeta.setDisplayName("§b§l " + team.getTeamName());
        chunkMeta.setLore(buildStatsLore(claimedChunk));
        chunkMeta.setEnchantmentGlintOverride(true);
        chunk.setItemMeta(chunkMeta);
        circleCenter.addItem(new GuiItem(chunk, event -> {
            //logic
        }));

        // FILLER CIRCLE
        paneel.bindItem('1', new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

        gui.addPane(Slot.fromXY(0, 0), background);
        gui.addPane(Slot.fromXY(1, 1), navigationPane);
        gui.addPane(Slot.fromXY(0, 0), paneel);
        gui.addPane(Slot.fromXY(6, 2), circleCenter);

        gui.show(player);
    }

    private List<String> buildStatsLore(ClaimedChunk claimedChunk) {
        ConfigurationSection levels = ChunkBlock.getInstance().getLevelsConfig().getConfigurationSection("levels");

        int level = claimedChunk.getLevel();
        int maxLevel = levels != null ? levels.getKeys(false).size() : level;
        int chunks = claimedChunk.getChunksPerSide();
        int size = chunks * 16;

        String ownerName = "Unknown";
        if (claimedChunk.getOwner() != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(claimedChunk.getOwner());
            if (owner.getName() != null) ownerName = owner.getName();
        }

        Location home = claimedChunk.getHome();
        String homeText = home != null
                ? "§7X: §f" + home.getBlockX() + " §7Y: §f" + home.getBlockY() + " §7Z: §f" + home.getBlockZ()
                : "§cNot set";

        String line = "§8§m" + " ".repeat(30);

        List<String> lore = new ArrayList<>();
        lore.add(line);
        lore.add(" §7Owner: §f" + ownerName);
        lore.add("");

        lore.add(" §b§lLEVEL");
        lore.add("  §7Level: §b" + level + " §8/ §7" + maxLevel);
        lore.add("  " + progressBar(level, maxLevel));
        lore.add("  §7XP: §f" + format((long) claimedChunk.getXp()));
        lore.add("");

        lore.add(" §a§lAREA");
        lore.add("  §7Size: §f" + chunks + "×" + chunks + " chunks §8(" + size + "×" + size + ")");
        lore.add("  §7Surface: §f" + format((long) size * size) + " blocks");
        lore.add("  §7Home: " + homeText);
        lore.add("");

        if (levels != null && level < maxLevel) {
            int next = level + 1;
            int nextChunks = Math.max(1, levels.getInt(next + ".chunkSize", size) / 16);
            lore.add(" §6§lNEXT LEVEL");
            lore.add("  §7Cost: §6$" + format(levels.getLong(next + ".cost")));
            lore.add("  §7New size: §f" + nextChunks + "×" + nextChunks + " chunks");
        } else {
            lore.add(" §a§l✔ MAX LEVEL REACHED");
        }

        lore.add(line);
        return lore;
    }

    private String progressBar(int value, int max) {
        double ratio = max > 0 ? Math.min(1.0, (double) value / max) : 0;
        int filled = (int) Math.round(ratio * 20);
        return "§8[§a" + "|".repeat(filled) + "§7" + "|".repeat(20 - filled) + "§8] §7" + (int) (ratio * 100) + "%";
    }

    private String format(long number) {
        return String.format(Locale.US, "%,d", number);
    }

    // FILLER LIGHT BLUE
    Pattern patroon = new Pattern(
            "000000000",
            "000001110",
            "000001010",
            "000001110",
            "000000000"
    );
}