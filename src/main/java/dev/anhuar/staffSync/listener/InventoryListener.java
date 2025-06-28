package dev.anhuar.staffSync.listener;

/*
 * ========================================================
 * StaffSync - InventoryListener.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 28/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import dev.anhuar.staffSync.util.menu.IMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        final Inventory topInventory = event.getView().getTopInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final Player player = (Player) event.getWhoClicked();

        if (!(topInventory.getHolder() instanceof IMenu menu)) return;

        menu.onClick(player, event);

        if (clickedInventory != null && clickedInventory.equals(topInventory)) {
            menu.click(player, event.getSlot(), event);
        }
    }

    @EventHandler
    public void onDragEvent(InventoryDragEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getWhoClicked();

        if (!(inventory.getHolder() instanceof IMenu menu)) return;

        menu.onDrag(player, event);
    }

    @EventHandler
    public void onCloseEvent(InventoryCloseEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getPlayer();

        if (!(inventory.getHolder() instanceof IMenu menu)) return;

        menu.onClose(player, event);
    }
}