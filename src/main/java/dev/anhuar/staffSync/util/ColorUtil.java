package dev.anhuar.staffSync.util;

/*
 * ========================================================
 * StaffSync - ColorUtil.java
 *
 * @author Anhuar Ruiz | Anhuar Dev | myclass
 * @web https://anhuar.dev
 * @date 29/06/2025
 *
 * License: MIT License - See LICENSE file for details.
 * Copyright (c) 2025 Anhuar Dev. All rights reserved.
 * ========================================================
 */

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class ColorUtil {

    public static String format(String text) {
        if (text == null) return "";

        if (containsMiniMessageTags(text)) {
            Component component = MiniMessage.miniMessage().deserialize(text);
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        return translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', text));
    }

    private static String translateHexColorCodes(String message) {
        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})|#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher matcher = hexPattern.matcher(message);

        StringBuffer buffer = new StringBuffer(message.length() + 32);

        while (matcher.find()) {
            String hexCode = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            StringBuilder replacement = new StringBuilder("§x");

            for (char c : hexCode.toCharArray()) {
                replacement.append("§").append(c);
            }

            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static boolean containsMiniMessageTags(String text) {
        return text.contains("<") && text.contains(">");
    }
}