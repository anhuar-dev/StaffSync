package dev.anhuar.staffSync.handler;

/*
 * ========================================================
 * StaffSync - ListenerHandler.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 28/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import dev.anhuar.staffSync.StaffSync;
import dev.anhuar.staffSync.listener.InventoryListener;
import dev.anhuar.staffSync.listener.PlayerListener;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class ListenerHandler {

    private final StaffSync plugin;

    public ListenerHandler(StaffSync plugin) {
        this.plugin = plugin;
        registerListeners();
    }

    private void registerListeners() {
        List<Listener> listeners = Arrays.asList(
                new PlayerListener(plugin),
                new InventoryListener()
        );

        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}