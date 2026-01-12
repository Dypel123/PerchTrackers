package me.perch.util;

import me.perch.Trackers;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemUtil {

    private static final NumberFormat COMMA_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public static boolean hasTracker(ItemStack item, String trackerId) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(Trackers.getInstance(), "tracker_stat_" + trackerId);
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    public static int getTrackerStat(ItemStack item, String trackerId) {
        if (item == null || !item.hasItemMeta()) return 0;
        NamespacedKey key = new NamespacedKey(Trackers.getInstance(), "tracker_stat_" + trackerId);
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static void incrementTracker(ItemStack item, String trackerId) {
        increaseTracker(item, trackerId, 1);
    }

    public static void increaseTracker(ItemStack item, String trackerId, int amount) {
        if (item == null || !item.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(Trackers.getInstance(), "tracker_stat_" + trackerId);
        ItemMeta meta = item.getItemMeta();

        int current = meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, current + amount);

        item.setItemMeta(meta);
    }

    public static void removeTracker(ItemStack item, String trackerId, String loreFormatRaw) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Trackers.getInstance(), "tracker_stat_" + trackerId);

        meta.getPersistentDataContainer().remove(key);

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            String prefixRaw = loreFormatRaw.split("\\{")[0];
            String cleanPrefix = ColorUtil.colorize(prefixRaw);

            lore.removeIf(line -> line.contains(cleanPrefix));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
    }

    public static void updateItemLore(ItemStack item, String trackerId, String loreFormat) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        int count = getTrackerStat(item, trackerId);

        String countFormatted = COMMA_FORMAT.format(count);

        String finalLine = ColorUtil.colorize(loreFormat.replace("{count_commas}", countFormatted));

        String prefixRaw = loreFormat.split("\\{")[0];
        String cleanPrefix = ColorUtil.colorize(prefixRaw);
        lore.removeIf(line -> line.contains(cleanPrefix));

        lore.add(finalLine);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}