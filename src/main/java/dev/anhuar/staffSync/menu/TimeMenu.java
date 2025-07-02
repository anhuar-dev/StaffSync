package dev.anhuar.staffSync.menu;

/*
 * ========================================================
 * StaffSync - TimeMenu.java
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
import dev.anhuar.staffSync.manager.TimeManager;
import dev.anhuar.staffSync.util.ColorUtil;
import dev.anhuar.staffSync.util.menu.MenuUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimeMenu extends MenuUtil {
    private final StaffSync plugin;
    private final String playerName;
    private final UUID playerUUID;
    private int currentPage = 0;

    public TimeMenu(StaffSync plugin, String playerName, UUID playerUUID) {
        super(plugin.getStaffTimeMenu().getConfig().getInt("TIME-MENU.SIZE", 6),
                plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.TITLE").replace("{player}", playerName));
        this.plugin = plugin;
        this.playerName = playerName;
        this.playerUUID = playerUUID;

        this.setClickAction((player, event) -> event.setCancelled(true));
        this.setDragAction((player, event) -> event.setCancelled(true));
    }

    @Override
    public void setItems() {
        clearItems();

        // Agregar decoración
        addDecorativeItems();

        // Obtener historial de tiempo
        List<TimeManager.TimeRecord> timeHistory = plugin.getManagerHandler().getTimeManager().getTimeHistory(playerUUID);

        if (timeHistory.isEmpty()) {
            // Mostrar mensaje de "sin historial"
            addNoHistoryItem();
            return;
        }

        // Mostrar registros paginados
        displayTimeRecords(timeHistory);

        // Agregar navegación de páginas si es necesario
        addNavigationItems(timeHistory);
    }

    private void addDecorativeItems() {
        // Agregar bordes
        if (plugin.getStaffTimeMenu().getConfig().getBoolean("TIME-MENU.DECORATION.BORDER.ENABLED")) {
            String materialName = plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.DECORATION.BORDER.MATERIAL");
            String name = plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.DECORATION.BORDER.NAME");

            Material material = Material.valueOf(materialName);
            ItemStack borderItem = createItem(material, name, null);

            for (String line : plugin.getStaffTimeMenu().getConfig().getStringList("TIME-MENU.DECORATION.BORDER.SLOTS")) {
                String[] slotStrs = line.replace(" ", "").split(",");
                for (String slotStr : slotStrs) {
                    try {
                        setItem(Integer.parseInt(slotStr), borderItem);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // Botón volver
        String path = "TIME-MENU.DECORATION.CUSTOM-ITEMS.BACK";
        if (plugin.getStaffTimeMenu().getConfig().getBoolean(path + ".ENABLED")) {
            Material material = Material.valueOf(plugin.getStaffTimeMenu().getConfig().getString(path + ".MATERIAL"));
            String name = plugin.getStaffTimeMenu().getConfig().getString(path + ".NAME");
            List<String> lore = plugin.getStaffTimeMenu().getConfig().getStringList(path + ".LORE");
            int slot = plugin.getStaffTimeMenu().getConfig().getInt(path + ".SLOT");

            ItemStack item = createItem(material, name, lore);
            setItem(slot, item, (player, event) -> {
                event.setCancelled(true);
                new MainMenu(plugin).openMenu(player);
            });
        }
    }

    private void addNoHistoryItem() {
        String name = plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.NO-HISTORY.NAME");
        List<String> lore = plugin.getStaffTimeMenu().getConfig().getStringList("TIME-MENU.NO-HISTORY.LORE");
        int slot = plugin.getStaffTimeMenu().getConfig().getInt("TIME-MENU.NO-HISTORY.SLOT");

        ItemStack item = createItem(Material.BARRIER, name, lore);
        setItem(slot, item);
    }

    private void displayTimeRecords(List<TimeManager.TimeRecord> timeHistory) {
        List<Integer> slots = plugin.getStaffTimeMenu().getConfig().getIntegerList("TIME-MENU.TIME-SLOTS");
        String materialName = plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.TIME-ITEM.MATERIAL");
        String nameFormat = plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.TIME-ITEM.NAME");
        String dateFormat = plugin.getStaffTimeMenu().getConfig().getString("TIME-MENU.TIME-ITEM.DATE-FORMAT");
        List<String> loreFormat = plugin.getStaffTimeMenu().getConfig().getStringList("TIME-MENU.TIME-ITEM.LORE");

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

        // Calcular páginas
        int recordsPerPage = slots.size();
        int startIndex = currentPage * recordsPerPage;
        int endIndex = Math.min(startIndex + recordsPerPage, timeHistory.size());

        // Mostrar registros de esta página
        for (int i = startIndex; i < endIndex; i++) {
            TimeManager.TimeRecord record = timeHistory.get(i);
            int slotIndex = i - startIndex;

            // Formatear fecha
            String formattedDate;
            try {
                formattedDate = sdf.format(parser.parse(record.getDate()));
            } catch (ParseException e) {
                formattedDate = record.getDate();
            }

            List<String> lore = new ArrayList<>();
            for (String line : loreFormat) {
                lore.add(line.replace("{time}", formatTime(record.getTime())));
            }

            ItemStack item = createItem(
                    Material.valueOf(materialName),
                    nameFormat.replace("{date}", formattedDate),
                    lore
            );

            setItem(slots.get(slotIndex), item);
        }
    }

    private void addNavigationItems(List<TimeManager.TimeRecord> timeHistory) {
        int recordsPerPage = plugin.getStaffTimeMenu().getConfig().getIntegerList("TIME-MENU.TIME-SLOTS").size();
        int totalPages = (int) Math.ceil((double) timeHistory.size() / recordsPerPage);

        if (totalPages <= 1) return;

        // Botón página anterior
        if (currentPage > 0) {
            String path = "TIME-MENU.NAVIGATION.PREVIOUS";
            ItemStack item = createItem(
                    Material.valueOf(plugin.getStaffTimeMenu().getConfig().getString(path + ".MATERIAL")),
                    plugin.getStaffTimeMenu().getConfig().getString(path + ".NAME"),
                    plugin.getStaffTimeMenu().getConfig().getStringList(path + ".LORE")
            );

            setItem(plugin.getStaffTimeMenu().getConfig().getInt(path + ".SLOT"), item, (player, event) -> {
                event.setCancelled(true);
                currentPage--;
                setItems();
            });
        }

        // Botón página siguiente
        if (currentPage < totalPages - 1) {
            String path = "TIME-MENU.NAVIGATION.NEXT";
            ItemStack item = createItem(
                    Material.valueOf(plugin.getStaffTimeMenu().getConfig().getString(path + ".MATERIAL")),
                    plugin.getStaffTimeMenu().getConfig().getString(path + ".NAME"),
                    plugin.getStaffTimeMenu().getConfig().getStringList(path + ".LORE")
            );

            setItem(plugin.getStaffTimeMenu().getConfig().getInt(path + ".SLOT"), item, (player, event) -> {
                event.setCancelled(true);
                currentPage++;
                setItems();
            });
        }

        // Indicador de página
        String path = "TIME-MENU.NAVIGATION.PAGE-INDICATOR";
        String format = plugin.getStaffTimeMenu().getConfig().getString(path + ".FORMAT");
        ItemStack item = createItem(
                Material.valueOf(plugin.getStaffTimeMenu().getConfig().getString(path + ".MATERIAL")),
                format.replace("{current}", String.valueOf(currentPage + 1))
                        .replace("{total}", String.valueOf(totalPages)),
                plugin.getStaffTimeMenu().getConfig().getStringList(path + ".LORE")
        );

        setItem(plugin.getStaffTimeMenu().getConfig().getInt(path + ".SLOT"), item);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.format(name));

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ColorUtil.format(line));
                }
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }

        return item;
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