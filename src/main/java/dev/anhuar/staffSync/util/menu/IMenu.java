package dev.anhuar.staffSync.util.menu;

/*
 * ========================================================
 * StaffSync - IMenu.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 28/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public interface IMenu extends InventoryHolder {

    void click(Player player, int slot, InventoryClickEvent event);

    void onClick(Player player, InventoryClickEvent event);

    void onDrag(Player player, InventoryDragEvent event);

    void middleClick(Player player, int slot, InventoryClickEvent event);

    void onClose(Player player, InventoryCloseEvent event);

    void setItem(int slot, ItemStack itemStack);

    void setItem(int slot, ItemStack itemStack, BiConsumer<Player, InventoryClickEvent> clickAction);

    void setItems();

    default void open(Player player) {
        setItems();

        player.openInventory(getInventory());
    }
}