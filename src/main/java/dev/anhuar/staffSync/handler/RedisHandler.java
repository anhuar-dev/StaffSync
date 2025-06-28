package dev.anhuar.staffSync.handler;

/*
 * ========================================================
 * StaffSync - RedisHandler.java
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class RedisHandler {

    private final StaffSync plugin;
    private JedisPool pool;

    public RedisHandler(StaffSync plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        try {
            String host = plugin.getSetting().getString("REDIS.HOST");
            int port = plugin.getSetting().getInt("REDIS.PORT");
            String password = plugin.getSetting().getString("REDIS.PASSWORD");

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);

            if (password != null && !password.isEmpty()) {
                pool = new JedisPool(config, host, port, 0, password);
            } else {
                pool = new JedisPool(config, host, port, 0);
            }

            plugin.getLogger().info("Conexión con Redis establecida correctamente!");
        } catch (Exception e) {
            plugin.getLogger().severe("Error al conectar con Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publish(String channel, String message) {
        if (pool == null) return;

        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        } catch (Exception e) {
            plugin.getLogger().warning("Error al publicar mensaje en Redis: " + e.getMessage());
        }
    }

    public void subscribe(String channel, Consumer<String> messageHandler) {
        if (pool == null) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> messageHandler.accept(message));
                    }
                }, channel);
            } catch (Exception e) {
                plugin.getLogger().warning("Error en la suscripción Redis: " + e.getMessage());
                // Intentar reconectar después de un error
                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () ->
                        subscribe(channel, messageHandler), 200L);
            }
        });
    }

    public void disconnect() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
            plugin.getLogger().info("Conexión con Redis cerrada correctamente");
        }
    }
}