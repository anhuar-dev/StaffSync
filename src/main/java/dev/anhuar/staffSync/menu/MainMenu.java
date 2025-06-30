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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class MainMenu extends MenuUtil {
    private final StaffSync plugin;

    public MainMenu(StaffSync plugin) {
        super(6, plugin.getStaffMenu().getString("MAIN-MENU.TITLE"));
        this.plugin = plugin;

        this.setClickAction((player, event) -> event.setCancelled(true));
        this.setDragAction((player, event) -> event.setCancelled(true));
    }

    @Override
    public void setItems() {
        clearItems();

        addDecorativeItems();

        List<DPlayer> staffMembers = plugin.getManagerHandler().getPlayerDataManager().getAllStaffMembers();

        StaffValidationResult result = validateStaffMembers(staffMembers);

        removeInvalidStaffMembers(result.membersToRemove);

        displayStaffHeads(result.validStaffMembers);
    }

    private StaffValidationResult validateStaffMembers(List<DPlayer> staffMembers) {
        List<DPlayer> validStaffMembers = new ArrayList<>();
        List<UUID> membersToRemove = new ArrayList<>();

        for (DPlayer staffMember : staffMembers) {
            processStaffMember(staffMember, membersToRemove, validStaffMembers);
        }

        return new StaffValidationResult(validStaffMembers, membersToRemove);
    }

    private void processStaffMember(DPlayer staffMember, List<UUID> membersToRemove, List<DPlayer> validStaffMembers) {
        UUID uuid = UUID.fromString(staffMember.getUuid());
        Player player = Bukkit.getPlayer(uuid);

        if (player == null || !player.isOnline()) {
            if (staffMember.hasStaffPermission()) {
                validStaffMembers.add(staffMember);
            }
            return;
        }

        boolean hasStaffPerm = player.hasPermission("staff.magnament");

        if (!hasStaffPerm && staffMember.hasStaffPermission()) {
            membersToRemove.add(uuid);
            return;
        }

        if (hasStaffPerm) {
            updateStaffPermissionIfNeeded(staffMember, uuid);
            validStaffMembers.add(staffMember);
        }
    }

    private void updateStaffPermissionIfNeeded(DPlayer staffMember, UUID uuid) {
        if (!staffMember.hasStaffPermission()) {
            staffMember.setHasStaffPermission(true);
            plugin.getManagerHandler().getPlayerDataManager().save(uuid);
        }
    }

    private void removeInvalidStaffMembers(List<UUID> membersToRemove) {
        if (membersToRemove.isEmpty()) {
            return;
        }

        for (UUID uuid : membersToRemove) {
            plugin.getManagerHandler().getPlayerDataManager().removeStaffMember(uuid);
        }
    }

    private void displayStaffHeads(List<DPlayer> validStaffMembers) {
        List<Integer> staffSlots = plugin.getStaffMenu().getConfig().getIntegerList("MAIN-MENU.STAFF-SLOTS");

        int maxStaffToShow = Math.min(validStaffMembers.size(), staffSlots.size());
        for (int i = 0; i < maxStaffToShow; i++) {
            setItem(staffSlots.get(i), createStaffHead(validStaffMembers.get(i)));
        }
    }

    private void addDecorativeItems() {
        addBorderItemsIfEnabled();
        addCustomItems();
    }

    private void addBorderItemsIfEnabled() {
        if (!plugin.getStaffMenu().getBoolean("MAIN-MENU.DECORATION.BORDER.ENABLED")) {
            return;
        }

        String materialName = plugin.getStaffMenu().getString("MAIN-MENU.DECORATION.BORDER.MATERIAL");
        String name = plugin.getStaffMenu().getString("MAIN-MENU.DECORATION.BORDER.NAME");
        List<Integer> slots = getBorderSlots();

        ItemStack borderItem = createNamedItem(Material.valueOf(materialName), name);

        for (int slot : slots) {
            setItem(slot, borderItem);
        }
    }

    private List<Integer> getBorderSlots() {
        List<String> slotStrings = plugin.getStaffMenu().getConfig().getStringList("MAIN-MENU.DECORATION.BORDER.SLOTS");
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

    private void addCustomItems() {
        String basePath = "MAIN-MENU.DECORATION.CUSTOM-ITEMS";
        if (!plugin.getStaffMenu().getConfig().isConfigurationSection(basePath)) {
            return;
        }

        Map<String, BiConsumer<Integer, ItemStack>> actionHandlers = createActionHandlers();
        BiConsumer<Integer, ItemStack> defaultAction = this::setItem;

        for (String key : plugin.getStaffMenu().getConfig().getConfigurationSection(basePath).getKeys(false)) {
            String path = basePath + "." + key;

            if (!plugin.getStaffMenu().getBoolean(path + ".ENABLED")) {
                continue;
            }

            String materialName = plugin.getStaffMenu().getString(path + ".MATERIAL");
            String name = plugin.getStaffMenu().getConfig().getString(path + ".NAME", "&f" + key);
            List<String> lore = plugin.getStaffMenu().getConfig().getStringList(path + ".LORE");
            int slot = plugin.getStaffMenu().getInt(path + ".SLOT");
            String action = plugin.getStaffMenu().getConfig().getString(path + ".ACTION", "");

            ItemStack item = createItem(Material.valueOf(materialName), name, lore);
            actionHandlers.getOrDefault(action, defaultAction).accept(slot, item);
        }
    }

    private Map<String, BiConsumer<Integer, ItemStack>> createActionHandlers() {
        Map<String, BiConsumer<Integer, ItemStack>> actionHandlers = new HashMap<>();

        actionHandlers.put("refresh", (slot, item) -> setItem(slot, item, (player, event) -> {
            event.setCancelled(true);
            player.sendMessage("§a[StaffSync] §fActualizando lista de staff...");
            open(player);
        }));

        actionHandlers.put("close", (slot, item) -> setItem(slot, item, (player, event) -> {
            event.setCancelled(true);
            player.closeInventory();
        }));

        return actionHandlers;
    }

    private ItemStack createStaffHead(DPlayer staffMember) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(staffMember.getUuid())));

        String nameFormat = plugin.getStaffMenu().getConfig().getString("MAIN-MENU.STAFF-HEAD.NAME", "&e{name}");
        meta.setDisplayName(nameFormat.replace("{name}", staffMember.getName()).replace("&", "§"));

        meta.setLore(createStaffHeadLore(staffMember));

        head.setItemMeta(meta);
        return head;
    }

    private List<String> createStaffHeadLore(DPlayer staffMember) {
        List<String> loreFormat = plugin.getStaffMenu().getConfig().getStringList("MAIN-MENU.STAFF-HEAD.LORE");
        List<String> lore = new ArrayList<>();

        boolean isOnline = staffMember.isOnline();
        String statusText = getStatusText(staffMember, isOnline);
        String formattedTime = formatTime(staffMember.getDailyTime());
        String lastSeen = formatDate(staffMember.getLastSeen());

        for (String line : loreFormat) {
            if (line.contains("{online}")) {
                addStatusLines(lore, statusText);
            } else {
                lore.add(replacePlaceholders(line, staffMember, formattedTime, lastSeen));
            }
        }

        return lore;
    }

    private String getStatusText(DPlayer staffMember, boolean isOnline) {
        if (isOnline) {
            String template = plugin.getStaffMenu().getString("MAIN-MENU.STAFF-HEAD.ONLINE-TEXT");
            return template.replace("{current-server}", staffMember.getCurrentServer());
        }

        return plugin.getStaffMenu().getString("MAIN-MENU.STAFF-HEAD.OFFLINE-TEXT");
    }

    private void addStatusLines(List<String> lore, String statusText) {
        for (String statusLine : statusText.split("\n")) {
            lore.add(statusLine.replace("&", "§"));
        }
    }

    private String replacePlaceholders(String line, DPlayer staffMember, String formattedTime, String lastSeen) {
        return line.replace("{daily-time}", formattedTime).replace("{last-server}", staffMember.getLastServer()).replace("{last-seen}", lastSeen).replace("&", "§");
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    private ItemStack createNamedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
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

    private static class StaffValidationResult {
        private final List<DPlayer> validStaffMembers;
        private final List<UUID> membersToRemove;

        public StaffValidationResult(List<DPlayer> validStaffMembers, List<UUID> membersToRemove) {
            this.validStaffMembers = validStaffMembers;
            this.membersToRemove = membersToRemove;
        }
    }
}