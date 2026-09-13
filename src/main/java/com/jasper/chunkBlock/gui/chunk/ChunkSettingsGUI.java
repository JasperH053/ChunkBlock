package com.jasper.chunkBlock.gui.chunk;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.chunk.settings.Setting;
import com.jasper.chunkBlock.chunk.settings.SettingsManager;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.util.MessageUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ChunkSettingsGUI {

    private final Player player;
    private final Team team;
    private final ClaimedChunk claimedChunk;
    private final SettingsManager settingsManager;

    public ChunkSettingsGUI(Player player, Team team, ClaimedChunk claimedChunk, SettingsManager settingsManager) {
        this.player = player;
        this.team = team;
        this.claimedChunk = claimedChunk;
        this.settingsManager = settingsManager;

        if (claimedChunk == null) {
            throw new IllegalStateException("Chunk not found: " + team.getTeamName());
        }
    }

    public void open() {
        ChestGui gui = new ChestGui(4, "Chunk - Settings");

        PaginatedPane pages = new PaginatedPane(9, 3);
        OutlinePane settingsPane = new OutlinePane(9, 3);

        ProtectedRegion region = claimedChunk.getRegion();

        if (region == null) {
            MessageUtils.sendError(player, "Did not find region");
            return;
        }

        World bukkitWorld = Bukkit.getWorld(claimedChunk.getWorldName());
        RegionManager manager = (bukkitWorld != null) ? WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitWorld)) : null;

        for (Setting setting : settingsManager.getAvailableSettings().values()) {
            com.sk89q.worldguard.protection.flags.Flag<?> rawFlag = WorldGuard.getInstance().getFlagRegistry().get(setting.getId());

            if (!(rawFlag instanceof StateFlag)) {
                continue;
            }

            StateFlag flag = (StateFlag) rawFlag;

            StateFlag.State state = region.getFlag(flag);
            boolean enabled = (state == StateFlag.State.ALLOW);

            // GECORRIGEERD: Geef het hele setting object mee
            ItemStack itemIcon = getSettingIcon(setting, enabled);

            GuiItem clickableItem = new GuiItem(itemIcon, event -> {
                event.setCancelled(true);

                boolean newEnabled = !enabled;
                region.setFlag(flag, newEnabled ? StateFlag.State.ALLOW : StateFlag.State.DENY);

                if (manager != null) {
                    manager.addRegion(region);
                }

                claimedChunk.toggleSetting(setting);

                new ChunkSettingsGUI(player, team, claimedChunk, settingsManager).open();
            });

            settingsPane.addItem(clickableItem);
        }

        pages.addPane(0, Slot.fromXY(0, 0), settingsPane);
        gui.addPane(Slot.fromXY(0, 0), pages);

        OutlinePane background = new OutlinePane(9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(Slot.fromXY(0, 3), background);

        StaticPane navigation = new StaticPane(9, 1);
        navigation.addItem(new GuiItem(new ItemStack(Material.RED_WOOL), event -> {
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                gui.update();
            }
        }), Slot.fromXY(0, 0));

        navigation.addItem(new GuiItem(new ItemStack(Material.GREEN_WOOL), event -> {
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        }), Slot.fromXY(8, 0));

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cBack");
            barrier.setItemMeta(meta);
        }

        navigation.addItem(new GuiItem(barrier, event -> {
            ChunkMainGUI ch = new ChunkMainGUI(player, team);
            ch.open();
        }), Slot.fromXY(4, 0));

        gui.addPane(Slot.fromXY(0, 3), navigation);
        gui.show(player);
    }

    // GECORRIGEERD: Vraagt nu om het 'Setting' object, converteert de String naar een Bukkit Material en haalt de displaynaam correct op.
    private @NotNull ItemStack getSettingIcon(Setting setting, boolean state) {
        Material mat = Material.matchMaterial(setting.getMaterial() != null ? setting.getMaterial().toUpperCase() : "STONE");
        if (mat == null) mat = Material.STONE; // Veilige fallback als de YAML een typfout bevat

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(setting.getDisplayName() + " §8» " + (state ? "§aTRUE" : "§cFALSE"));
            item.setItemMeta(meta);
        }
        return item;
    }
}