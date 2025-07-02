package dev.anhuar.staffSync.manager;

/*
 * ========================================================
 * StaffSync - TimeManager.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 29/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import dev.anhuar.staffSync.StaffSync;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimeManager {
    private final StaffSync plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public TimeManager(StaffSync plugin) {
        this.plugin = plugin;
    }

    /**
     * Guarda el tiempo diario de un jugador en el historial
     */
    public void saveTimeRecord(UUID uuid, long dailyTime) {
        if (dailyTime <= 0) return;

        String today = dateFormat.format(new Date());

        Document timeRecord = new Document();
        timeRecord.put("date", today);
        timeRecord.put("time", dailyTime);

        // Verificar si ya existe un registro para hoy
        Document existingRecord = plugin.getMongoHandler().getPlayers().find(
                Filters.and(
                        Filters.eq("_id", uuid.toString()),
                        Filters.eq("timeRecords.date", today)
                )
        ).first();

        if (existingRecord != null) {
            // Actualizar registro existente
            plugin.getMongoHandler().getPlayers().updateOne(
                    Filters.eq("_id", uuid.toString()),
                    Updates.set("timeRecords.$[elem].time", dailyTime),
                    new com.mongodb.client.model.UpdateOptions()
                            .arrayFilters(Collections.singletonList(Filters.eq("elem.date", today)))
            );
        } else {
            // Crear nuevo registro
            plugin.getMongoHandler().getPlayers().updateOne(
                    Filters.eq("_id", uuid.toString()),
                    Updates.push("timeRecords", timeRecord)
            );
        }
    }

    /**
     * Obtiene el historial de tiempo de un jugador
     */
    public List<TimeRecord> getTimeHistory(UUID uuid) {
        List<TimeRecord> records = new ArrayList<>();

        Document player = plugin.getMongoHandler().getPlayers().find(
                Filters.eq("_id", uuid.toString())
        ).first();

        if (player == null || !player.containsKey("timeRecords")) {
            return records;
        }

        @SuppressWarnings("unchecked")
        List<Document> timeRecords = (List<Document>) player.get("timeRecords");

        for (Document record : timeRecords) {
            String date = record.getString("date");
            long time = record.getLong("time");
            records.add(new TimeRecord(date, time));
        }

        // Ordenar por fecha (más reciente primero)
        records.sort((r1, r2) -> r2.getDate().compareTo(r1.getDate()));

        return records;
    }

    public static class TimeRecord {
        private final String date;
        private final long time;

        public TimeRecord(String date, long time) {
            this.date = date;
            this.time = time;
        }

        public String getDate() {
            return date;
        }

        public long getTime() {
            return time;
        }
    }
}