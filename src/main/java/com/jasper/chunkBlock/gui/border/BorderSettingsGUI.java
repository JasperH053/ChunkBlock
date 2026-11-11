package com.jasper.chunkBlock.gui.border;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.gui.chunk.ChunkMainGUI;
import com.jasper.chunkBlock.gui.chunk.ChunkSettingsGUI;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Locale;

public class BorderSettingsGUI {

    private final Player player;
    private ClaimedChunk claimedChunk;
    private TeamService teamService;


    public BorderSettingsGUI(Player player, TeamService teamService, ClaimedChunk claimedChunk) {
        this.player = player;
        this.claimedChunk = claimedChunk;
        this.teamService = teamService;
    }

    public void open() {
        HopperGui gui = new HopperGui("Border - Settings");

        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnGlobalDrag(event -> event.setCancelled(true));

        StaticPane pane = new StaticPane(9, 1);

        String[] arr = { "Red", "Green", "Blue" };

        for (int i = 0; i < arr.length; i++) {
            final ClaimedChunk.BorderColor color = ClaimedChunk.BorderColor.valueOf(arr[i].toUpperCase());

            ItemStack woolItem = new ItemStack(Material.valueOf(arr[i].toUpperCase() + "_WOOL"));
            ItemMeta colorMeta = woolItem.getItemMeta();
            colorMeta.setDisplayName("§7" + arr[i]);
            woolItem.setItemMeta(colorMeta);

            pane.addItem(new GuiItem(woolItem, event -> {
                claimedChunk.applyBorder(player, color);
                open();
            }), Slot.fromXY(i, 0));
        }

        pane.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)), 3, 0);

        boolean borderOn = player.getWorldBorder() != null;

        ItemStack toggle = new ItemStack(borderOn ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = toggle.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(borderOn ? "§7Border: §aON" : "§7Border: §cOFF");
            toggle.setItemMeta(meta);
        }

        pane.addItem(new GuiItem(toggle, event -> {
            if (player.getWorldBorder() != null) {
                claimedChunk.removeBorder(player);
            } else {
                claimedChunk.applyBorder(player);
            }
            open();
        }), Slot.fromXY(4, 0));

        gui.getSlotsComponent().addPane(Slot.fromXY(0, 0), pane);
        gui.show(player);
    }

}