package me.perch.listeners;

import me.perch.Trackers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertOldTrackersListener implements Listener {

    private final Trackers plugin;
    private final Map<String, String> conversionMap = new HashMap<>();

    public ConvertOldTrackersListener(Trackers plugin) {
        this.plugin = plugin;
        initializeMappings();
    }

    private void initializeMappings() {
        conversionMap.put("arrows shot", "arrows_shot");
        conversionMap.put("blocks broken", "blocks_broken");
        conversionMap.put("blocks placed", "blocks_placed");
        conversionMap.put("bosses killed", "bosses_killed");
        conversionMap.put("damage dealt", "damage_dealt");
        conversionMap.put("distance sneaked", "distance_sneaked");
        conversionMap.put("enderman killed", "endermen_killed");
        conversionMap.put("fish caught", "fish_caught");
        conversionMap.put("headshots", "headshots");
        conversionMap.put("mobs killed", "mob_kills");
        conversionMap.put("players killed", "players_killed");
        conversionMap.put("pumpkins harvested", "pumpkins_farmed");
        conversionMap.put("times jumped", "jumps");
        conversionMap.put("times voted", "votes");
        conversionMap.put("fall damage", "fall_damage");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                convertInventory(event.getPlayer());
            }
        }, 20L);
    }

    public void convertInventory(Player player) {
        boolean playerUpdated = false;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            if (convertItem(contents[i], player)) {
                playerUpdated = true;
                player.getInventory().setItem(i, contents[i]);
            }
        }

        if (playerUpdated) {
            player.sendMessage(ChatColor.GREEN + "Trackers " + ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Your old trackers have been converted! Once you gain a point in the tracker, it will automatically update on the item.");
            player.updateInventory();
        }
    }

    private boolean convertItem(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        NamespacedKey trackerIdKey = new NamespacedKey(plugin, "tracker_id");
        if (meta.getPersistentDataContainer().has(trackerIdKey, PersistentDataType.STRING)) {
            return false;
        }

        if (!meta.hasLore() || meta.getLore() == null) return false;

        List<String> originalLore = meta.getLore();
        List<String> newLore = new ArrayList<>();
        boolean changed = false;

        for (String line : originalLore) {

            String stripped = line.replaceAll("§.", "").trim();
            String lowerStripped = stripped.toLowerCase();

            boolean isTrackerLine = false;

            for (Map.Entry<String, String> entry : conversionMap.entrySet()) {
                String oldName = entry.getKey();

                if (lowerStripped.startsWith(oldName)) {
                    String newId = entry.getValue();
                    NamespacedKey statKey = new NamespacedKey(plugin, "tracker_stat_" + newId);

                    if (meta.getPersistentDataContainer().has(statKey, PersistentDataType.INTEGER)) {
                        break;
                    }

                    String remainder = stripped.substring(oldName.length());
                    String numberStr = remainder.replace(",", "").replaceAll("[^0-9.]", "");

                    if (!numberStr.isEmpty()) {
                        try {
                            double dVal = Double.parseDouble(numberStr);
                            int value = (int) Math.round(dVal);

                            meta.getPersistentDataContainer().set(
                                    statKey,
                                    PersistentDataType.INTEGER,
                                    value
                            );

                            changed = true;
                            isTrackerLine = true;

                            plugin.getLogger().info("Converted Item Stat: " + oldName + " -> " + value + " for " + player.getName());

                            break;
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Failed to parse number for " + oldName + ": " + numberStr);
                        }
                    }
                }
            }

            if (!isTrackerLine) {
                newLore.add(line);
            }
        }

        if (changed) {
            meta.setLore(newLore);
            item.setItemMeta(meta);
            return true;
        }

        return false;
    }
}