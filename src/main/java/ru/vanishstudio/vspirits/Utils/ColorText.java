package ru.vanishstudio.vspirits.Utils;

import org.bukkit.ChatColor;

public class ColorText {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
