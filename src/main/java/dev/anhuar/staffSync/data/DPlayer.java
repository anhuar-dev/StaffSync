package dev.anhuar.staffSync.data;

/*
 * ========================================================
 * StaffSync - DPlayer.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 28/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DPlayer {
    private String uuid;
    private String name;
    private boolean online;
    private String currentServer;
    private String lastServer;
    private long lastSeen;
    private long dailyTime;
    private boolean hasStaffPermission;

    public DPlayer() {
        this.online = false;
        this.currentServer = "";
        this.lastServer = "";
        this.lastSeen = System.currentTimeMillis();
        this.dailyTime = 0L;
        this.hasStaffPermission = false;
    }

    public void addDailyTime(long timeToAdd) {
        this.dailyTime += timeToAdd;
    }

    public boolean hasStaffPermission() {
        return hasStaffPermission;
    }
}