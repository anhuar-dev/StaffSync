package dev.anhuar.staffSync.command;

/*
 * ========================================================
 * StaffSync - AdminCommand.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 29/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import dev.anhuar.staffSync.StaffSync;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("staffsync")
@CommandPermission("staffsync.admin")
public class AdminCommand {

    private final StaffSync plugin;

    public AdminCommand(StaffSync plugin) {
        this.plugin = plugin;
    }

    @DefaultFor("staffsync")
    public void onAdminCommand() {
    }

    @Subcommand("reload")
    public void onReloadCommand(Player player) {
        plugin.getSetting().reload();
        plugin.getMessage().reload();
        plugin.getStaffMenu().reload();
        plugin.getDemoteMenu().reload();
        plugin.getPromoteMenu().reload();
        player.sendMessage(ChatColor.GREEN + "StaffSync has been reloaded!");
    }
}