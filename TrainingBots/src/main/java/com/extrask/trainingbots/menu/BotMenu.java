package com.extrask.trainingbots.menu;

import com.extrask.trainingbots.config.BotConfiguration;
import com.extrask.trainingbots.util.ItemFactory;
import com.extrask.trainingbots.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BotMenu {
    private final Player player;
    private final BotConfiguration configuration;
    private boolean slowFalling;
    private boolean immortality;
    private boolean shield;
    private boolean glowing;
    private boolean lookAtPlayer;
    private boolean helmet;
    private boolean chestplate;
    private boolean leggings;
    private boolean boots;
    private Inventory inventory;

    public BotMenu(Player player, BotOptions options, BotConfiguration configuration) {
        this.player = player;
        this.configuration = configuration;
        this.slowFalling = options.slowFalling();
        this.immortality = options.immortality();
        this.shield = options.shield();
        this.glowing = options.glowing();
        this.lookAtPlayer = options.lookAtPlayer();
        this.helmet = options.helmet();
        this.chestplate = options.chestplate();
        this.leggings = options.leggings();
        this.boots = options.boots();
    }

    public void open() {
        inventory = Bukkit.createInventory(null, 54, configuration.menuTitle());
        redraw();
        player.openInventory(inventory);
    }

    public void toggleSlowFalling() {
        slowFalling = !slowFalling;
        redraw();
    }

    public void toggleImmortality() {
        immortality = !immortality;
        redraw();
    }

    public void toggleShield() {
        shield = !shield;
        redraw();
    }

    public void toggleGlowing() {
        glowing = !glowing;
        redraw();
    }

    public void toggleLookAtPlayer() {
        lookAtPlayer = !lookAtPlayer;
        redraw();
    }

    public void toggleHelmet() {
        helmet = !helmet;
        redraw();
    }

    public void toggleChestplate() {
        chestplate = !chestplate;
        redraw();
    }

    public void toggleLeggings() {
        leggings = !leggings;
        redraw();
    }

    public void toggleBoots() {
        boots = !boots;
        redraw();
    }

    public boolean slowFalling() {
        return slowFalling;
    }

    public boolean immortality() {
        return immortality;
    }

    public boolean shield() {
        return shield;
    }

    public boolean glowing() {
        return glowing;
    }

    public boolean lookAtPlayer() {
        return lookAtPlayer;
    }

    public BotOptions options() {
        return new BotOptions(slowFalling, immortality, shield, glowing, lookAtPlayer, helmet, chestplate, leggings, boots);
    }

    public Inventory inventory() {
        return inventory;
    }

    public boolean isAtSlot(String item, int slot) {
        return configuration.menuItem(item).slot() == slot;
    }

    private void redraw() {
        inventory.clear();
        MenuItemSettings frameSettings = configuration.menuFrame();
        List<Integer> frameSlots = configuration.frameSlots();
        if (frameSlots.isEmpty()) {
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                inventory.setItem(slot, createItem(frameSettings, Map.of()));
            }
        } else {
            for (int slot : frameSlots) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, createItem(frameSettings, Map.of()));
                }
            }
        }
        setItem("helmet", armorButton("helmet", helmet));
        setItem("slow-falling", button("slow-falling", slowFalling));
        setItem("immortality", button("immortality", immortality));
        setItem("shield", button("shield", shield));
        setItem("chestplate", armorButton("chestplate", chestplate));
        setItem("glowing", button("glowing", glowing));
        setItem("look-at-player", button("look-at-player", lookAtPlayer));
        setItem("leggings", armorButton("leggings", leggings));
        setItem("boots", armorButton("boots", boots));
        setItem("spawn", createItem(configuration.menuItem("spawn"), Map.of()));
    }

    private void setItem(String key, ItemStack item) {
        int slot = configuration.menuItem(key).slot();
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }

    private ItemStack button(String key, boolean enabled) {
        return createItem(configuration.menuItem(key), Map.of("{status}", enabled ? "&a&lON" : "&c&lOFF"));
    }

    private ItemStack armorButton(String key, boolean blastProtection) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{status}", blastProtection ? "&a&lON" : "&c&lOFF");
        placeholders.put("{enchantment}", blastProtection ? "&aBlast Protection IV" : "&bProtection IV");
        return createItem(configuration.menuItem(key), placeholders);
    }

    private ItemStack createItem(MenuItemSettings settings, Map<String, String> placeholders) {
        ItemStack item = ItemFactory.simple(settings.material());
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(replace(settings.name(), placeholders)));
            meta.setLore(settings.lore().stream().map(line -> Text.color(replace(line, placeholders))).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    private String replace(String value, Map<String, String> placeholders) {
        String replaced = value;
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            replaced = replaced.replace(placeholder.getKey(), placeholder.getValue());
        }
        return replaced;
    }
}