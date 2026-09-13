package com.jasper.chunkBlock.gui.base;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.Material;
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
        ChestGui gui = new ChestGui(3, title);

        // AANGEPAST: Posities (0, 1) verwijderd uit constructor
        StaticPane pane = new StaticPane(9, 1);

        // YES button (green)
        ItemStack yesItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta yesMeta = yesItem.getItemMeta();
        yesMeta.setDisplayName("§aYes");
        yesItem.setItemMeta(yesMeta);

        // AANGEPAST: Posities (2, 0) verpakt in Slot.fromXY
        pane.addItem(new GuiItem(yesItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            onConfirm();
        }), Slot.fromXY(2, 0));

        // NO button (red)
        ItemStack noItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = noItem.getItemMeta();
        noMeta.setDisplayName("§cNo");
        noItem.setItemMeta(noMeta);

        // AANGEPAST: Posities (6, 0) verpakt in Slot.fromXY
        pane.addItem(new GuiItem(noItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            onDeny();
        }), Slot.fromXY(6, 0));

        // AANGEPAST: gui.addPane geplaatst op rij 1 (y: 1) via Slot.fromXY
        gui.addPane(Slot.fromXY(0, 1), pane);
        gui.show(player);
    }

    protected abstract void onConfirm();
    protected abstract void onDeny();
}