package dev.anhuar.staffSync.menu;

/*
 * ========================================================
 * StaffSync - DemoteMenu.java
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
import dev.anhuar.staffSync.util.ColorUtil;
import dev.anhuar.staffSync.util.menu.MenuUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DemoteMenu extends MenuUtil {
    private final StaffSync plugin;
    private final String targetName;

    public DemoteMenu(StaffSync plugin, String targetName) {
        super(
                plugin.getStaffDemoteMenu().getConfig().getInt("DEMOTE-MENU.SIZE", 4),
                plugin.getStaffDemoteMenu().getConfig().getString("DEMOTE-MENU.TITLE")
        );
        this.plugin = plugin;
        this.targetName = targetName;

        this.setClickAction((player, event) -> event.setCancelled(true));
        this.setDragAction((player, event) -> event.setCancelled(true));
    }

    @Override
    public void setItems() {
        clearItems();
        addDecorativeItems();
        addRankItems();
    }

    private void addDecorativeItems() {
        if (plugin.getStaffDemoteMenu().getConfig().getBoolean("DEMOTE-MENU.DECORATION.BORDER.ENABLED")) {
            String materialName = plugin.getStaffDemoteMenu().getConfig().getString("DEMOTE-MENU.DECORATION.BORDER.MATERIAL");
            String name = plugin.getStaffDemoteMenu().getConfig().getString("DEMOTE-MENU.DECORATION.BORDER.NAME");
            List<Integer> slots = getBorderSlots();

            ItemStack borderItem = createNamedItem(Material.valueOf(materialName), name);

            for (int slot : slots) {
                setItem(slot, borderItem);
            }
        }

        String basePath = "DEMOTE-MENU.DECORATION.CUSTOM-ITEMS";
        if (!plugin.getStaffDemoteMenu().getConfig().isConfigurationSection(basePath)) {
            return;
        }

        for (String key : plugin.getStaffDemoteMenu().getConfig().getConfigurationSection(basePath).getKeys(false)) {
            String path = basePath + "." + key;

            if (!plugin.getStaffDemoteMenu().getConfig().getBoolean(path + ".ENABLED")) {
                continue;
            }

            String materialName = plugin.getStaffDemoteMenu().getConfig().getString(path + ".MATERIAL");
            String name = plugin.getStaffDemoteMenu().getConfig().getString(path + ".NAME", "&f" + key);
            List<String> lore = plugin.getStaffDemoteMenu().getConfig().getStringList(path + ".LORE");
            int slot = plugin.getStaffDemoteMenu().getConfig().getInt(path + ".SLOT");
            String action = plugin.getStaffDemoteMenu().getConfig().getString(path + ".ACTION", "");

            ItemStack item = createItem(Material.valueOf(materialName), name, lore);

            if ("back".equals(action)) {
                setItem(slot, item, (player, event) -> {
                    event.setCancelled(true);
                    new MainMenu(plugin).openMenu(player);
                });
            } else {
                setItem(slot, item);
            }
        }
    }

    private void addRankItems() {
        String basePath = "DEMOTE-MENU.RANKS";
        if (!plugin.getStaffDemoteMenu().getConfig().isConfigurationSection(basePath)) {
            return;
        }

        Set<String> rankKeys = plugin.getStaffDemoteMenu().getConfig().getConfigurationSection(basePath).getKeys(false);

        for (String rankKey : rankKeys) {
            String path = basePath + "." + rankKey;

            if (!plugin.getStaffDemoteMenu().getConfig().getBoolean(path + ".ENABLED")) {
                continue;
            }

            String materialName = plugin.getStaffDemoteMenu().getConfig().getString(path + ".MATERIAL");
            String name = plugin.getStaffDemoteMenu().getConfig().getString(path + ".NAME");
            List<String> loreConfig = plugin.getStaffDemoteMenu().getConfig().getStringList(path + ".LORE");
            int slot = plugin.getStaffDemoteMenu().getConfig().getInt(path + ".SLOT");
            String rank = plugin.getStaffDemoteMenu().getConfig().getString(path + ".RANK");

            List<String> lore = new ArrayList<>();
            for (String line : loreConfig) {
                lore.add(line.replace("{player}", targetName));
            }

            ItemStack item = createItem(Material.valueOf(materialName), name, lore);

            setItem(slot, item, (player, event) -> {
                event.setCancelled(true);

                String command = "lp user " + targetName + " parent remove " + rank;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                player.sendMessage("§a[StaffSync] §fSe ha degradado a §e" + targetName + " §fquitándole el rango §e" + rank);
                player.closeInventory();
            });
        }
    }

    private List<Integer> getBorderSlots() {
        List<String> slotStrings = plugin.getStaffDemoteMenu().getConfig().getStringList("DEMOTE-MENU.DECORATION.BORDER.SLOTS");
        List<Integer> slots = new ArrayList<>();

        for (String slotLine : slotStrings) {
            String[] slotArray = slotLine.replace(" ", "").split(",");
            for (String slotStr : slotArray) {
                try {
                    slots.add(Integer.parseInt(slotStr));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Valor de slot no válido: " + slotStr);
                }
            }
        }

        return slots;
    }

    private ItemStack createNamedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.format(name));
            item.setItemMeta(meta);
        }
        return item;
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

    public void openMenu(Player player) {
        open(player);
    }
}