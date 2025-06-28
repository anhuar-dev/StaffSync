package dev.anhuar.staffSync.menu;

/*
 * ========================================================
 * StaffSync - MainMenu.java
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
import dev.anhuar.staffSync.data.DPlayer;
import dev.anhuar.staffSync.util.menu.MenuUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainMenu extends MenuUtil {
    private final StaffSync plugin;

    public MainMenu(StaffSync plugin) {
        super(6, plugin.getMenu().getString("MAIN-MENU.TITLE")); // 6 filas = 54 slots
        this.plugin = plugin;

        // Configuramos comportamiento por defecto para cancelar interacciones
        this.setClickAction((player, event) -> event.setCancelled(true));
        this.setDragAction((player, event) -> event.setCancelled(true));
    }

    @Override
    public void setItems() {
        // Obtener todos los jugadores con permisos de staff según la base de datos
        List<DPlayer> staffMembers = plugin.getManagerHandler().getPlayerDataManager().getAllStaffMembers();
        List<DPlayer> validStaffMembers = new ArrayList<>();
        List<UUID> membersToRemove = new ArrayList<>();

        // Verificar permisos actuales
        for (DPlayer staffMember : staffMembers) {
            UUID uuid = UUID.fromString(staffMember.getUuid());
            Player player = Bukkit.getPlayer(uuid);
            boolean hasStaffPerm = false;

            // Para jugadores online, verificar permiso actual
            if (player != null && player.isOnline()) {
                hasStaffPerm = player.hasPermission("staff.magnament");

                // Si ya no tiene permiso, agregarlo a la lista de eliminación
                if (!hasStaffPerm && staffMember.hasStaffPermission()) {
                    membersToRemove.add(uuid);
                } else if (hasStaffPerm) {
                    // Actualizar el estado de permiso si es necesario
                    if (!staffMember.hasStaffPermission()) {
                        staffMember.setHasStaffPermission(true);
                        plugin.getManagerHandler().getPlayerDataManager().save(uuid);
                    }
                    validStaffMembers.add(staffMember);
                }
            } else if (staffMember.hasStaffPermission()) {
                // Para jugadores offline, confiar en los datos almacenados
                validStaffMembers.add(staffMember);
            }
        }

        // Eliminar miembros sin permisos de la base de datos
        if (!membersToRemove.isEmpty()) {
            for (UUID uuid : membersToRemove) {
                // Eliminar del caché
                plugin.getManagerHandler().getPlayerDataManager().removeStaffMember(uuid);
            }
        }

        // Mostrar solo los miembros válidos
        for (int i = 0; i < Math.min(validStaffMembers.size(), 54); i++) {
            DPlayer staffMember = validStaffMembers.get(i);
            setItem(i, createStaffHead(staffMember));
        }
    }

    private ItemStack createStaffHead(DPlayer staffMember) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        // Configurar cabeza del jugador
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(staffMember.getUuid())));
        meta.setDisplayName("§e" + staffMember.getName());

        // Crear lore con información del staff
        List<String> lore = new ArrayList<>();
        lore.add("§7§m------------------");

        boolean isOnline = staffMember.isOnline();
        if (isOnline) {
            lore.add("§aEstado: §fConectado");
            lore.add("§aServidor: §f" + staffMember.getCurrentServer());
        } else {
            lore.add("§cEstado: §fDesconectado");
        }

        lore.add("§eTiempo hoy: §f" + formatTime(staffMember.getDailyTime()));

        lore.add("§7§m------------------");
        lore.add("§bÚltimo servidor: §f" + staffMember.getLastServer());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String lastSeen = sdf.format(new Date(staffMember.getLastSeen()));
        lore.add("§bÚltima conexión: §f" + lastSeen);

        lore.add("§7§m------------------");

        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private String formatTime(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;

        return hours + "h " + minutes + "m";
    }

    public void openMenu(Player player) {
        open(player);
    }
}