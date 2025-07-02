package dev.anhuar.staffSync.manager;

/*
 * ========================================================
 * StaffSync - PlayerDataManager.java
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
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.anhuar.staffSync.StaffSync;
import dev.anhuar.staffSync.data.DPlayer;
import dev.anhuar.staffSync.util.GsonUtil;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class PlayerDataManager {

    private final Map<UUID, DPlayer> playerDataMap;
    private final Map<UUID, Long> sessionStartTimes;
    private final String serverName;

    public PlayerDataManager() {
        this.playerDataMap = Maps.newConcurrentMap();
        this.sessionStartTimes = Maps.newConcurrentMap();
        this.serverName = StaffSync.getInstance().getSetting().getString("SERVER.NAME");
        startTimeUpdater();
        subscribeToRedisUpdates();
    }

    public void load(UUID uuid) {
        Document document = StaffSync.getInstance().getMongoHandler().getPlayers().find(Filters.eq("_id", uuid.toString())).first();

        if (document == null) {
            DPlayer playerData = new DPlayer();
            String playerName = getPlayerName(uuid);
            playerData.setUuid(uuid.toString());
            playerData.setName(playerName);
            playerData.setOnline(false);
            playerData.setCurrentServer("");
            playerData.setLastServer("");
            playerData.setLastSeen(System.currentTimeMillis());
            playerData.setDailyTime(0L);

            // Verificar si tiene permiso de staff (si está online)
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                playerData.setHasStaffPermission(player.hasPermission("staff.magnament"));
            }

            playerDataMap.put(uuid, playerData);
            save(uuid);
            return;
        }

        Document data = document.get("data", Document.class);
        Object object = GsonUtil.parseJsonString(data.toJson(GsonUtil.getWriterSettings()), DPlayer.class);
        DPlayer playerData = (DPlayer) object;

        // Actualizar nombre si es necesario
        String currentName = getPlayerName(uuid);
        if (!currentName.equals(playerData.getName())) {
            playerData.setName(currentName);
        }

        playerDataMap.put(uuid, playerData);
    }

    public void save(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(StaffSync.getInstance(), () -> {
            DPlayer playerData = playerDataMap.get(uuid);
            if (playerData == null) return;

            // Guardar datos generales del jugador
            String jsonData = GsonUtil.getGson().toJson(playerData);
            Document document = Document.parse(jsonData);
            Document newDocument = new Document();
            newDocument.put("_id", uuid.toString());
            newDocument.put("data", document);

            // Guardar registro de tiempo diario en historial
            if (playerData.getDailyTime() > 0) {
                StaffSync.getInstance().getManagerHandler().getTimeManager().saveTimeRecord(uuid, playerData.getDailyTime());
            }

            StaffSync.getInstance().getMongoHandler().getPlayers().replaceOne(
                    Filters.eq("_id", uuid.toString()),
                    newDocument,
                    new ReplaceOptions().upsert(true)
            );
        });
    }

    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        // Cargar datos del jugador si no están en caché
        if (!playerDataMap.containsKey(uuid)) {
            load(uuid);
        }

        DPlayer playerData = getPlayerData(uuid);
        if (playerData == null) return;

        // Actualizar información
        boolean hasStaffPerm = player.hasPermission("staff.magnament");
        playerData.setHasStaffPermission(hasStaffPerm);
        playerData.setOnline(true);
        playerData.setCurrentServer(serverName);
        playerData.setLastServer(serverName);
        playerData.setLastSeen(System.currentTimeMillis());

        // Guardar tiempo de inicio de sesión
        sessionStartTimes.put(uuid, System.currentTimeMillis());

        // Publicar actualización a Redis si es staff
        if (hasStaffPerm) {
            Map<String, String> data = new HashMap<>();
            data.put("uuid", uuid.toString());
            data.put("action", "join");
            data.put("server", serverName);
            StaffSync.getInstance().getRedisHandler().publish(StaffSync.getInstance().getSetting().getString("REDIS.CHANNEL"), GsonUtil.getGson().toJson(data));
        }

        save(uuid);
    }

    public void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        DPlayer playerData = getPlayerData(uuid);
        if (playerData == null) return;

        // Actualizar tiempo online
        if (sessionStartTimes.containsKey(uuid)) {
            long sessionTime = System.currentTimeMillis() - sessionStartTimes.get(uuid);
            playerData.addDailyTime(sessionTime);
            sessionStartTimes.remove(uuid);
        }

        // Actualizar datos
        playerData.setOnline(false);
        playerData.setCurrentServer("");
        playerData.setLastSeen(System.currentTimeMillis());

        // Publicar actualización a Redis si es staff
        if (playerData.hasStaffPermission()) {
            Map<String, String> data = new HashMap<>();
            data.put("uuid", uuid.toString());
            data.put("action", "quit");
            StaffSync.getInstance().getRedisHandler().publish(StaffSync.getInstance().getSetting().getString("REDIS.CHANNEL"), GsonUtil.getGson().toJson(data));
        }

        save(uuid);
    }

    public List<DPlayer> getAllStaffMembers() {
        List<DPlayer> staffList = new ArrayList<>();

        // Primero buscamos en caché
        for (DPlayer player : playerDataMap.values()) {
            if (player.hasStaffPermission()) {
                staffList.add(player);
            }
        }

        // Luego complementamos con la base de datos
        FindIterable<Document> documents = StaffSync.getInstance().getMongoHandler().getPlayers()
                .find(Filters.eq("data.hasStaffPermission", true));

        for (Document doc : documents) {
            String uuidStr = doc.getString("_id");
            UUID uuid = UUID.fromString(uuidStr);

            // Si no está en caché, lo agregamos
            if (!playerDataMap.containsKey(uuid)) {
                Document data = doc.get("data", Document.class);
                Object object = GsonUtil.parseJsonString(data.toJson(GsonUtil.getWriterSettings()), DPlayer.class);
                DPlayer playerData = (DPlayer) object;
                staffList.add(playerData);
                playerDataMap.put(uuid, playerData);
            }
        }

        return staffList;
    }

    public DPlayer getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    private String getPlayerName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public void removeStaffMember(UUID uuid) {
        // Eliminar de la base de datos
        Bukkit.getScheduler().runTaskAsynchronously(StaffSync.getInstance(), () -> {
            StaffSync.getInstance().getMongoHandler().getPlayers().deleteOne(
                    Filters.eq("_id", uuid.toString())
            );

            // Eliminar del caché si existe
            playerDataMap.remove(uuid);
            sessionStartTimes.remove(uuid);

            // Notificar a otros servidores a través de Redis
            Map<String, String> data = new HashMap<>();
            data.put("uuid", uuid.toString());
            data.put("action", "staff_removed");
            StaffSync.getInstance().getRedisHandler().publish(
                    StaffSync.getInstance().getSetting().getString("REDIS.CHANNEL"),
                    GsonUtil.getGson().toJson(data)
            );

            StaffSync.getInstance().getLogger().info("Jugador " + uuid + " eliminado de la base de datos de staff.");
        });
    }

    private void startTimeUpdater() {
        // Actualizar tiempo online cada minuto
        Bukkit.getScheduler().runTaskTimerAsynchronously(StaffSync.getInstance(), () -> {
            long now = System.currentTimeMillis();

            // Actualizar tiempo para jugadores online
            for (Map.Entry<UUID, Long> entry : sessionStartTimes.entrySet()) {
                UUID uuid = entry.getKey();
                if (playerDataMap.containsKey(uuid)) {
                    DPlayer player = playerDataMap.get(uuid);
                    long sessionTime = now - entry.getValue();
                    player.addDailyTime(sessionTime);
                    save(uuid);
                    sessionStartTimes.put(uuid, now); // Reiniciar contador
                }
            }

            // Revisar cambio de día y reiniciar contadores
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) <= 1) {
                resetDailyTimes();
            }
        }, 1200L, 1200L); // 60 segundos
    }

    private void resetDailyTimes() {
        // Obtener todos los jugadores con datos
        FindIterable<Document> documents = StaffSync.getInstance().getMongoHandler().getPlayers().find();

        for (Document doc : documents) {
            String uuidStr = doc.getString("_id");
            UUID uuid = UUID.fromString(uuidStr);

            // Actualizar en caché
            if (playerDataMap.containsKey(uuid)) {
                DPlayer player = playerDataMap.get(uuid);
                player.setDailyTime(0L);
                save(uuid);
            }
            // Actualizar en base de datos
            else {
                Document data = doc.get("data", Document.class);
                Document newData = Document.parse(data.toJson());
                newData.put("dailyTime", 0L);

                Document newDoc = new Document();
                newDoc.put("_id", uuidStr);
                newDoc.put("data", newData);

                StaffSync.getInstance().getMongoHandler().getPlayers().replaceOne(
                        Filters.eq("_id", uuidStr),
                        newDoc,
                        new ReplaceOptions().upsert(true)
                );
            }
        }
    }

    private void subscribeToRedisUpdates() {
        StaffSync.getInstance().getRedisHandler().subscribe(StaffSync.getInstance().getSetting().getString("REDIS.CHANNEL"), message -> {
            try {
                Map<String, String> data = GsonUtil.getGson().fromJson(message, Map.class);
                if (data == null || !data.containsKey("uuid")) return;

                String uuidStr = data.get("uuid");
                UUID uuid = UUID.fromString(uuidStr);
                String action = data.get("action");

                if ("staff_removed".equals(action)) {
                    // Eliminar del caché local cuando otro servidor envía notificación
                    playerDataMap.remove(uuid);
                    sessionStartTimes.remove(uuid);
                    StaffSync.getInstance().getLogger().info("Jugador " + uuid + " eliminado localmente por solicitud de otro servidor");
                    return;
                }

                // Actualizar datos de jugador según el evento
                DPlayer playerData = playerDataMap.getOrDefault(uuid, null);
                if (playerData == null) {
                    load(uuid);
                    playerData = playerDataMap.get(uuid);
                    if (playerData == null) return;
                }

                if ("join".equals(action)) {
                    String server = data.get("server");
                    playerData.setOnline(true);
                    playerData.setCurrentServer(server);
                    playerData.setLastServer(server);
                    playerData.setLastSeen(System.currentTimeMillis());
                } else if ("quit".equals(action)) {
                    playerData.setOnline(false);
                    playerData.setCurrentServer("");
                    playerData.setLastSeen(System.currentTimeMillis());
                }
            } catch (Exception e) {
                StaffSync.getInstance().getLogger().warning("Error procesando mensaje Redis: " + e.getMessage());
            }
        });
    }
}