package dev.anhuar.staffSync.util.menu;

/*
 * ========================================================
 * StaffSync - MenuUtil.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 28/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import com.google.common.collect.Maps;
import dev.anhuar.staffSync.StaffSync;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

@Getter
public abstract class MenuUtil implements IMenu {

    private final Inventory inventory;
    private final MiniMessage miniMessage;

    private final Map<Integer, BiConsumer<Player, InventoryClickEvent>> playerActions;

    @Setter
    private BiConsumer<Player, InventoryClickEvent> clickAction;
    @Setter
    private BiConsumer<Player, InventoryDragEvent> dragAction;
    @Setter
    private BiConsumer<Player, InventoryCloseEvent> closeAction;

    @Getter
    public final StaffSync instance = StaffSync.getInstance();

    public MenuUtil(int size, String title) {
        this.miniMessage = MiniMessage.miniMessage();
        this.playerActions = Maps.newConcurrentMap();

        this.inventory = Bukkit.createInventory(
                this,
                size * 9,
                miniMessage.deserialize(title)
                        .decoration(TextDecoration.ITALIC, false)
        );
    }

    @Override
    public void click(Player player, int slot, InventoryClickEvent event) {
        final BiConsumer<Player, InventoryClickEvent> action = playerActions.get(slot);

        if (action == null) return;

        action.accept(player, event);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if (clickAction != null) {
            clickAction.accept(player, event);
        }
    }

    @Override
    public void onDrag(Player player, InventoryDragEvent event) {
        if (dragAction != null) {
            dragAction.accept(player, event);
        }
    }

    @Override
    public void onClose(Player player, InventoryCloseEvent event) {
        if (closeAction != null) {
            closeAction.accept(player, event);
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        setItem(slot, itemStack, (player, event) -> event.setCancelled(true));
    }

    @Override
    public void setItem(int slot, ItemStack itemStack, BiConsumer<Player, InventoryClickEvent> clickAction) {
        this.playerActions.put(slot, clickAction);

        this.inventory.setItem(slot, itemStack);
    }

    public abstract void setItems();

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void clearItems() {
        this.inventory.clear();
        this.playerActions.clear();
    }
}