package dev.anhuar.staffSync.command;

/*
 * ========================================================
 * StaffSync - ManagementCommand.java
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
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("management")
@CommandPermission("staffsync.management")
public class ManagementCommand {

    private final StaffSync plugin;

    public ManagementCommand(StaffSync plugin) {
        this.plugin = plugin;
    }

    @DefaultFor("management")
    public void onManagementCommand(Player player) {

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.sendMessage("§a[StaffSync] §fAbriendo el menú de gestión...");
            plugin.getMainMenu().openMenu(player);
        });
    }
}