package com.jasper.chunkBlock.gui.base;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ConfirmationGUI {

    private final Player player;
    private final String title;

    public ConfirmationGUI(Player player, String title) {
        this.player = player;
        this.title = title;
    }

    public void open() {
        HopperGui gui = new HopperGui(title);

        StaticPane pane = new StaticPane(9, 1);

        // YES button (green)
        ItemStack yesItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta yesMeta = yesItem.getItemMeta();
        yesMeta.setDisplayName("§aYes");
        yesItem.setItemMeta(yesMeta);

        pane.addItem(new GuiItem(yesItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            onConfirm();
        }), Slot.fromXY(0, 0));

        // NO button (red)
        ItemStack noItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = noItem.getItemMeta();
        noMeta.setDisplayName("§cNo");
        noItem.setItemMeta(noMeta);

        pane.addItem(new GuiItem(noItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            onDeny();
        }), Slot.fromXY(4, 0));

        gui.getSlotsComponent().addPane(Slot.fromXY(0, 0), pane);
        gui.show(player);
    }

    protected abstract void onConfirm();
    protected abstract void onDeny();
}