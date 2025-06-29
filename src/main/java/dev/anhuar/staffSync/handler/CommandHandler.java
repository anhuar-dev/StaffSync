package dev.anhuar.staffSync.handler;

/*
 * ========================================================
 * StaffSync - CommandHandler.java
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
import dev.anhuar.staffSync.command.AdminCommand;
import dev.anhuar.staffSync.command.ManagementCommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class CommandHandler {

    private final StaffSync plugin;
    private final BukkitCommandHandler commandHandler;

    public CommandHandler(StaffSync plugin) {
        this.plugin = plugin;
        this.commandHandler = BukkitCommandHandler.create(plugin);
        commandHandler.registerDependency(StaffSync.class, plugin);

        registerCommands();
    }

    private void registerCommands() {
        commandHandler.register(new AdminCommand(plugin));
        commandHandler.register(new ManagementCommand(plugin));
    }
}