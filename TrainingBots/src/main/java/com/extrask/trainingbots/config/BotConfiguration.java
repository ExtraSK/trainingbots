package com.extrask.trainingbots.config;

import com.extrask.trainingbots.menu.BotOptions;
import com.extrask.trainingbots.menu.MenuItemSettings;
import com.extrask.trainingbots.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BotConfiguration {
    private final JavaPlugin plugin;
    private BotSettings settings;
    private Map<String, String> messages = Collections.emptyMap();
    private String menuTitle = "Crystalhub.org´s Bot GUI";
    private MenuItemSettings menuFrame = new MenuItemSettings(-1, "BLACK_STAINED_GLASS_PANE", " ", List.of());
    private List<Integer> frameSlots = List.of();
    private Map<String, MenuItemSettings> menuItems = Collections.emptyMap();

    public BotConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        ConfigurationSection bot = plugin.getConfig().getConfigurationSection("bot");
        if (bot == null) {
            throw new IllegalStateException("Missing bot configuration section");
        }
        ConfigurationSection skin = bot.getConfigurationSection("skin");
        settings = new BotSettings(
                bot.getString("default-name", "&cTraining Bot"),
                skin == null ? "Steve" : skin.getString("player-name", "Steve"),
                skin == null ? "" : skin.getString("texture", ""),
                skin == null ? "" : skin.getString("signature", ""),
                bot.getBoolean("always-slow-falling", true),
                bot.getBoolean("immortality", true),
                bot.getBoolean("respawn-after-death", true),
                Math.max(1L, bot.getLong("respawn-delay", 20L)),
                Math.max(1.0D, bot.getDouble("spawn-radius", 100.0D)),
                Math.max(1.0D, bot.getDouble("immortality-radius", 100.0D)),
                bot.getBoolean("double-blast-protection", true),
                bot.getBoolean("shield", false),
                bot.getBoolean("glowing", false),
                bot.getBoolean("look-at-player", false),
                Math.max(1.0D, bot.getDouble("existing-bot-radius", 60.0D)),
                bot.contains("disabled-worlds") ? bot.getStringList("disabled-worlds") : List.of("voidworlreald")
        );

        ConfigurationSection gui = plugin.getConfig().getConfigurationSection("gui");
        menuTitle = gui == null ? "Crystalhub.org´s Bot GUI" : gui.getString("title", "Crystalhub.org´s Bot GUI");
        ConfigurationSection frame = gui == null ? null : gui.getConfigurationSection("frame");
        menuFrame = new MenuItemSettings(-1,
                frame == null ? "BLACK_STAINED_GLASS_PANE" : frame.getString("material", "BLACK_STAINED_GLASS_PANE"),
                frame == null ? " " : frame.getString("name", " "),
                frame == null ? List.of() : frame.getStringList("lore"));
        frameSlots = gui == null ? List.of() : gui.getIntegerList("frame.slots");

        Map<String, MenuItemSettings> loadedMenuItems = new LinkedHashMap<>();
        loadedMenuItems.put("helmet", loadMenuItem(gui, "helmet", 11, "NETHERITE_HELMET", "&b&lHelmet", List.of("&7Status: {status}", "", "&7Sets the helmet enchantment.", "&7Enchantment: {enchantment}", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("slow-falling", loadMenuItem(gui, "slow-falling", 13, "FEATHER", "&b&lSlow Falling", List.of("&7Status: {status}", "", "&7Prevents fall damage for the bot.", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("immortality", loadMenuItem(gui, "immortality", 15, "TOTEM_OF_UNDYING", "&b&lOwner Immortality", List.of("&7Status: {status}", "", "&7Protects you while near your bot.", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("shield", loadMenuItem(gui, "shield", 17, "SHIELD", "&b&lShield Blocking", List.of("&7Status: {status}", "", "&7Makes the bot block with its shield.", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("chestplate", loadMenuItem(gui, "chestplate", 20, "NETHERITE_CHESTPLATE", "&b&lChestplate", List.of("&7Status: {status}", "", "&7Sets the chestplate enchantment.", "&7Enchantment: {enchantment}", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("glowing", loadMenuItem(gui, "glowing", 22, "GLOWSTONE_DUST", "&b&lGlow Effect", List.of("&7Status: {status}", "", "&7Adds a glowing outline to the bot.", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("look-at-player", loadMenuItem(gui, "look-at-player", 24, "PLAYER_HEAD", "&b&lTrack Players", List.of("&7Status: {status}", "", "&7Makes the bot face nearby players.", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("leggings", loadMenuItem(gui, "leggings", 29, "NETHERITE_LEGGINGS", "&b&lLeggings", List.of("&7Status: {status}", "", "&7Sets the leggings enchantment.", "&7Enchantment: {enchantment}", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("boots", loadMenuItem(gui, "boots", 38, "NETHERITE_BOOTS", "&b&lBoots", List.of("&7Status: {status}", "", "&7Sets the boots enchantment.", "&7Enchantment: {enchantment}", "", "&e➜ Click to change this setting.")));
        loadedMenuItems.put("spawn", loadMenuItem(gui, "spawn", 48, "DIAMOND", "&a&lSPAWN PRACTICE BOT", List.of("&7Ready to create your practice bot.", "", "&a➜ Click to spawn with these settings.")));
        menuItems = Collections.unmodifiableMap(loadedMenuItems);

        Map<String, String> loadedMessages = new LinkedHashMap<>();
        ConfigurationSection messageSection = plugin.getConfig().getConfigurationSection("messages");
        if (messageSection != null) {
            for (String key : messageSection.getKeys(false)) {
                loadedMessages.put(key, messageSection.getString(key, ""));
            }
        }
        messages = Collections.unmodifiableMap(loadedMessages);
    }

    public BotSettings settings() {
        return settings;
    }

    public String menuTitle() {
        return Text.color(menuTitle);
    }

    public MenuItemSettings menuFrame() {
        return menuFrame;
    }

    public List<Integer> frameSlots() {
        return frameSlots;
    }

    public MenuItemSettings menuItem(String key) {
        return menuItems.get(key);
    }

    public ConfigurationSection section(String path) {
        return plugin.getConfig().getConfigurationSection(path);
    }

    public String message(String key) {
        return Text.color(messages.getOrDefault(key, "&cMissing message: " + key));
    }

    public String message(String key, String fallback) {
        return Text.color(messages.getOrDefault(key, fallback));
    }

    public BotOptions options(UUID playerId) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("player-options." + playerId);
        BotSettings defaults = settings();
        return new BotOptions(
                section == null ? defaults.slowFalling() : section.getBoolean("slow-falling", defaults.slowFalling()),
                section == null ? defaults.immortality() : section.getBoolean("immortality", defaults.immortality()),
                section == null ? defaults.shield() : section.getBoolean("shield", defaults.shield()),
                section == null ? defaults.glowing() : section.getBoolean("glowing", defaults.glowing()),
                section == null ? defaults.lookAtPlayer() : section.getBoolean("look-at-player", defaults.lookAtPlayer()),
                section != null && section.getBoolean("helmet", false),
                section != null && section.getBoolean("chestplate", false),
                section == null || section.getBoolean("leggings", true),
                section == null || section.getBoolean("boots", true)
        );
    }

    public void saveOptions(UUID playerId, BotOptions options) {
        String path = "player-options." + playerId;
        plugin.getConfig().set(path + ".slow-falling", options.slowFalling());
        plugin.getConfig().set(path + ".immortality", options.immortality());
        plugin.getConfig().set(path + ".shield", options.shield());
        plugin.getConfig().set(path + ".glowing", options.glowing());
        plugin.getConfig().set(path + ".look-at-player", options.lookAtPlayer());
        plugin.getConfig().set(path + ".helmet", options.helmet());
        plugin.getConfig().set(path + ".chestplate", options.chestplate());
        plugin.getConfig().set(path + ".leggings", options.leggings());
        plugin.getConfig().set(path + ".boots", options.boots());
        plugin.saveConfig();
    }

    private MenuItemSettings loadMenuItem(ConfigurationSection gui, String key, int defaultSlot, String defaultMaterial,
                                          String defaultName, List<String> defaultLore) {
        ConfigurationSection item = gui == null ? null : gui.getConfigurationSection("items." + key);
        if (item == null) {
            return new MenuItemSettings(defaultSlot, defaultMaterial, defaultName, defaultLore);
        }
        return new MenuItemSettings(item.getInt("slot", defaultSlot), item.getString("material", defaultMaterial),
                item.getString("name", defaultName), item.contains("lore") ? item.getStringList("lore") : defaultLore);
    }
}