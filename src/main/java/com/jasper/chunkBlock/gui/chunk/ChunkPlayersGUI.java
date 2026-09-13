package com.jasper.chunkBlock.gui.chunk;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.jasper.chunkBlock.ChunkBlock;
import com.jasper.chunkBlock.chunk.ClaimedChunk;
import com.jasper.chunkBlock.team.Team;
import com.jasper.chunkBlock.team.TeamService;
import com.jasper.chunkBlock.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class ChunkPlayersGUI {

    private final Player player;
    private final Team team;
    private final ClaimedChunk claimedChunk;
    private TeamService teamManager;

    public ChunkPlayersGUI(Player player, Team team, ClaimedChunk claimedChunk) {
        this.player = player;
        this.team = team;
        this.claimedChunk = claimedChunk;
        this.teamManager = ChunkBlock.getInstance().getTeamService();
        if (claimedChunk == null) {
            throw new IllegalStateException("Chunk not found for: " + team.getTeamName());
        }
    }

    public void open() {
        ChestGui gui = new ChestGui(4, "Chunk - Players");

        // AANGEPAST: Posities (0, 0) verwijderd uit de constructors
        PaginatedPane pages = new PaginatedPane(9, 3);
        OutlinePane settingsPane = new OutlinePane(9, 3);

        for (UUID memberUUID : team.getMembersOfTeam()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(member);

            String rank;

            if (team.getOwner().equals(member)) {
                rank = "owner";
            } else {
                rank = "member";
            }

            meta.setDisplayName("§e" + member.getName() + " " + rank);
            meta.setLore(List.of("§7UUID: " + memberUUID.toString()));
            skull.setItemMeta(meta);

            GuiItem guiItem = new GuiItem(skull, event -> {
                event.setCancelled(true);
                MessageUtils.sendSuccess(player, team.getOwner().toString());
            });

            settingsPane.addItem(guiItem);
        }

        pages.addPane(0, Slot.fromXY(0, 0), settingsPane);
        gui.addPane(Slot.fromXY(0, 0), pages);

        // AANGEPAST: Posities (0, 3) verwijderd uit de constructor
        OutlinePane background = new OutlinePane(9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(Slot.fromXY(0, 3), background);

        // AANGEPAST: Posities (0, 3) verwijderd uit de constructor
        StaticPane navigation = new StaticPane(9, 1);

        // AANGEPAST: Slots toegevoegd via Slot.fromXY(x, y)
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
        meta.setDisplayName("§cBack");
        barrier.setItemMeta(meta);

        navigation.addItem(new GuiItem(barrier, event -> {
            ChunkMainGUI ch = new ChunkMainGUI(player, team);
            ch.open();
        }), Slot.fromXY(4, 0));

        gui.addPane(Slot.fromXY(0, 3), navigation);

        gui.show(player);
    }
}