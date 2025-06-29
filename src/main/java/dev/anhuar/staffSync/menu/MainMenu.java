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
import org.bukkit.inventory.meta.ItemMeta;
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
        super(6, plugin.getStaffMenu().getString("MAIN-MENU.TITLE"));
        this.plugin = plugin;

        // Configuramos comportamiento por defecto para cancelar interacciones
        this.setClickAction((player, event) -> event.setCancelled(true));
        this.setDragAction((player, event) -> event.setCancelled(true));
    }

    @Override
    public void setItems() {
        // Limpiar el inventario completamente antes de añadir nuevos elementos
        clearItems();

        // Añadir elementos decorativos configurados
        addDecorativeItems();

        // Obtener todos los jugadores con permisos de staff según la base de datos
        List<DPlayer> staffMembers = plugin.getManagerHandler().getPlayerDataManager().getAllStaffMembers();
        List<DPlayer> validStaffMembers = new ArrayList<>();
        List<UUID> membersToRemove = new ArrayList<>();

        // Verificar permisos actuales (código existente para validación)
        for (DPlayer staffMember : staffMembers) {
            UUID uuid = UUID.fromString(staffMember.getUuid());
            Player player = Bukkit.getPlayer(uuid);
            boolean hasStaffPerm = false;

            // Para jugadores online, verificar permiso actual
            if (player != null && player.isOnline()) {
                hasStaffPerm = player.hasPermission("staff.magnament");

                if (!hasStaffPerm && staffMember.hasStaffPermission()) {
                    membersToRemove.add(uuid);
                } else if (hasStaffPerm) {
                    if (!staffMember.hasStaffPermission()) {
                        staffMember.setHasStaffPermission(true);
                        plugin.getManagerHandler().getPlayerDataManager().save(uuid);
                    }
                    validStaffMembers.add(staffMember);
                }
            } else if (staffMember.hasStaffPermission()) {
                validStaffMembers.add(staffMember);
            }
        }

        // Eliminar miembros sin permisos de la base de datos
        if (!membersToRemove.isEmpty()) {
            for (UUID uuid : membersToRemove) {
                plugin.getManagerHandler().getPlayerDataManager().removeStaffMember(uuid);
            }
        }

        // Obtener los slots configurados para mostrar staff
        List<Integer> staffSlots = plugin.getStaffMenu().getConfig().getIntegerList("MAIN-MENU.STAFF-SLOTS");

        // Mostrar solo los miembros válidos en los slots configurados
        int maxStaffToShow = Math.min(validStaffMembers.size(), staffSlots.size());
        for (int i = 0; i < maxStaffToShow; i++) {
            setItem(staffSlots.get(i), createStaffHead(validStaffMembers.get(i)));
        }
    }

    private void addDecorativeItems() {
        // Añadir bordes si están habilitados
        if (plugin.getStaffMenu().getBoolean("MAIN-MENU.DECORATION.BORDER.ENABLED")) {
            String materialName = plugin.getStaffMenu().getString("MAIN-MENU.DECORATION.BORDER.MATERIAL");
            String name = plugin.getStaffMenu().getString("MAIN-MENU.DECORATION.BORDER.NAME");

            // Obtener la lista de strings y procesarla
            List<String> slotStrings = plugin.getStaffMenu().getConfig().getStringList("MAIN-MENU.DECORATION.BORDER.SLOTS");
            List<Integer> slots = new ArrayList<>();

            // Procesar cada línea que contiene slots separados por comas
            for (String slotLine : slotStrings) {
                // Eliminar espacios y dividir por comas
                String[] slotArray = slotLine.replace(" ", "").split(",");
                for (String slotStr : slotArray) {
                    try {
                        int slot = Integer.parseInt(slotStr);
                        slots.add(slot);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Valor de slot no válido: " + slotStr);
                    }
                }
            }

            ItemStack borderItem = new ItemStack(Material.valueOf(materialName));
            ItemMeta meta = borderItem.getItemMeta();
            meta.setDisplayName(name.replace("&", "§"));
            borderItem.setItemMeta(meta);

            for (int slot : slots) {
                setItem(slot, borderItem);
            }
        }

        // Añadir items personalizados
        if (plugin.getStaffMenu().getConfig().isConfigurationSection("MAIN-MENU.DECORATION.CUSTOM-ITEMS")) {
            for (String key : plugin.getStaffMenu().getConfig().getConfigurationSection("MAIN-MENU.DECORATION.CUSTOM-ITEMS").getKeys(false)) {
                String path = "MAIN-MENU.DECORATION.CUSTOM-ITEMS." + key;

                if (plugin.getStaffMenu().getBoolean(path + ".ENABLED")) {
                    String materialName = plugin.getStaffMenu().getString(path + ".MATERIAL");
                    String name = plugin.getStaffMenu().getConfig().getString(path + ".NAME", "&f" + key);
                    List<String> lore = plugin.getStaffMenu().getConfig().getStringList(path + ".LORE");
                    int slot = plugin.getStaffMenu().getInt(path + ".SLOT");
                    String action = plugin.getStaffMenu().getString(path + ".ACTION");

                    ItemStack item = new ItemStack(Material.valueOf(materialName));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(name.replace("&", "§"));

                    if (!lore.isEmpty()) {
                        List<String> coloredLore = new ArrayList<>();
                        for (String line : lore) {
                            coloredLore.add(line.replace("&", "§"));
                        }
                        meta.setLore(coloredLore);
                    }

                    item.setItemMeta(meta);

                    if ("refresh".equals(action)) {
                        setItem(slot, item, (player, event) -> {
                            event.setCancelled(true);
                            player.sendMessage("§a[StaffSync] §fActualizando lista de staff...");
                            open(player);
                        });
                    } else {
                        setItem(slot, item);
                    }
                }
            }
        }
    }

    private ItemStack createStaffHead(DPlayer staffMember) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        // Configurar cabeza del jugador
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(staffMember.getUuid())));

        // Obtener formato del nombre
        String nameFormat = plugin.getStaffMenu().getString("MAIN-MENU.STAFF-HEAD.NAME");
        meta.setDisplayName(nameFormat.replace("{name}", staffMember.getName()).replace("&", "§"));

        // Obtener formato del lore
        List<String> loreFormat = plugin.getStaffMenu().getConfig().getStringList("MAIN-MENU.STAFF-HEAD.LORE");
        List<String> lore = new ArrayList<>();

        // Estado online/offline
        boolean isOnline = staffMember.isOnline();
        String statusText;
        if (isOnline) {
            statusText = plugin.getStaffMenu().getConfig().getString("MAIN-MENU.STAFF-HEAD.ONLINE-TEXT");
            statusText = statusText.replace("{current-server}", staffMember.getCurrentServer());
        } else {
            statusText = plugin.getStaffMenu().getConfig().getString("MAIN-MENU.STAFF-HEAD.OFFLINE-TEXT");
        }

        // Formatear tiempo
        String formattedTime = formatTime(staffMember.getDailyTime());

        // Formatear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String lastSeen = sdf.format(new Date(staffMember.getLastSeen()));

        // Procesar el lore completo con placeholders
        for (String line : loreFormat) {
            if (line.contains("{online}")) {
                // Manejar el caso especial de estado online/offline que puede tener múltiples líneas
                for (String statusLine : statusText.split("\n")) {
                    lore.add(statusLine.replace("&", "§"));
                }
            } else {
                lore.add(line
                        .replace("{daily-time}", formattedTime)
                        .replace("{last-server}", staffMember.getLastServer())
                        .replace("{last-seen}", lastSeen)
                        .replace("&", "§"));
            }
        }

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