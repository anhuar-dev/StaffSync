package dev.anhuar.staffSync.handler;

/*
 * ========================================================
 * StaffSync - ManagerHandler.java
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
import dev.anhuar.staffSync.manager.PlayerDataManager;
import dev.anhuar.staffSync.manager.TimeManager;
import lombok.Getter;

@Getter
public class ManagerHandler {

    private final StaffSync plugin;

    private PlayerDataManager playerDataManager;
    private TimeManager timeManager;

    public ManagerHandler(StaffSync plugin) {
        this.plugin = plugin;
        registerManager();
    }

    public void registerManager() {
        this.playerDataManager = new PlayerDataManager();
        this.timeManager = new TimeManager(plugin);
    }
}