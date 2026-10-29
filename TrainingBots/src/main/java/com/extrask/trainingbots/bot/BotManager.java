package com.extrask.trainingbots.bot;

import com.extrask.trainingbots.TrainingBotsPlugin;
import com.extrask.trainingbots.config.BotConfiguration;
import com.extrask.trainingbots.config.BotSettings;
import com.extrask.trainingbots.menu.BotOptions;
import com.extrask.trainingbots.util.ItemFactory;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BotManager {
    private final TrainingBotsPlugin plugin;
    private final BotConfiguration configuration;
    private final Map<UUID, TrainingBot> bots = new HashMap<>();
    private BukkitTask maintenanceTask;
    private boolean shieldUseUnavailable;

    public BotManager(TrainingBotsPlugin plugin, BotConfiguration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
    }

    public boolean hasOtherPlayerNearby(Location location, Player excluded, double radius) {
        double radiusSquared = radius * radius;
        for (Player online : location.getWorld().getPlayers()) {
            if (getBot(online) != null) {
                continue;
            }
            if (!online.getUniqueId().equals(excluded.getUniqueId()) && online.getLocation().distanceSquared(location) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNearbyOwnedBot(Player player, double radius) {
        double radiusSquared = radius * radius;
        for (TrainingBot bot : bots.values()) {
            if (bot.ownerId().equals(player.getUniqueId()) && bot.npc().isSpawned()
                    && bot.location().getWorld().equals(player.getWorld())
                    && bot.location().distanceSquared(player.getLocation()) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    public boolean removeNearbyOwnedBot(Player player, double radius) {
        double radiusSquared = radius * radius;
        for (TrainingBot bot : bots.values().toArray(TrainingBot[]::new)) {
            if (bot.ownerId().equals(player.getUniqueId()) && bot.npc().isSpawned()
                    && bot.location().getWorld().equals(player.getWorld())
                    && bot.location().distanceSquared(player.getLocation()) <= radiusSquared) {
                remove(bot, false);
                return true;
            }
        }
        return false;
    }

    public boolean isSpawnWorldDisabled(Player player) {
        return configuration.settings().disabledWorlds().stream()
                .anyMatch(world -> world.equalsIgnoreCase(player.getWorld().getName()));
    }

    public TrainingBot spawn(Player owner, Location location, BotOptions options) {
        String name = configuration.settings().name().replace("%player_name%", owner.getName());
        NPC npc = CitizensAPI.getTemporaryNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.setUseMinecraftAI(false);
        npc.setProtected(false);
        npc.setAlwaysUseNameHologram(false);
        configureSkin(npc, owner);
        configureEquipment(npc, options);
        if (!npc.spawn(location)) {
            npc.destroy();
            return null;
        }
        npc.getNavigator().cancelNavigation();
        Entity entity = npc.getEntity();
        TrainingBot bot = new TrainingBot(entity.getUniqueId(), owner.getUniqueId(), location.clone(), options, npc);
        bots.put(bot.entityId(), bot);
        if (options.slowFalling() && entity instanceof Player player) {
            applySlowFalling(player);
        }
        entity.setGlowing(options.glowing());
        giveStarterPack(owner);
        return bot;
    }

    public void removeNearest(Player player) {
        TrainingBot nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (TrainingBot bot : bots.values()) {
            if (!bot.location().getWorld().equals(player.getWorld())) {
                continue;
            }
            double distance = bot.location().distanceSquared(player.getLocation());
            if (distance <= 100.0D && distance < nearestDistance) {
                nearest = bot;
                nearestDistance = distance;
            }
        }
        if (nearest != null) {
            remove(nearest, false);
            player.sendMessage(configuration.message("bot-removed"));
        } else {
            player.sendMessage(configuration.message("no-bot"));
        }
    }

    public TrainingBot getBot(Entity entity) {
        return bots.get(entity.getUniqueId());
    }

    public boolean shouldProtectOwner(Player player) {
        for (TrainingBot bot : bots.values()) {
            if (!bot.ownerId().equals(player.getUniqueId()) || !bot.options().immortality() || !bot.npc().isSpawned()) {
                continue;
            }
            if (bot.location().getWorld().equals(player.getWorld())
                    && bot.location().distanceSquared(player.getLocation())
                    <= configuration.settings().immortalityRadius() * configuration.settings().immortalityRadius()
                    && !hasOtherPlayerNearby(bot.location(), player, configuration.settings().immortalityRadius())) {
                return true;
            }
        }
        return false;
    }

    public void handleDeath(TrainingBot bot) {
        if (!bots.containsKey(bot.entityId())) {
            return;
        }
        bots.remove(bot.entityId());
        Location location = bot.location().clone();
        UUID ownerId = bot.ownerId();
        BotOptions options = bot.options();
        bot.npc().destroy();
        if (!configuration.settings().respawnAfterDeath()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null && location.getWorld() != null) {
                spawn(owner, location, new BotOptions(options.slowFalling(), false, options.shield(), options.glowing(),
                        options.lookAtPlayer(), options.helmet(), options.chestplate(), options.leggings(), options.boots()));
            }
        }, configuration.settings().respawnDelay());
    }

    public void startMaintenance() {
        maintenanceTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (TrainingBot bot : bots.values().toArray(TrainingBot[]::new)) {
                if (!bot.npc().isSpawned()) {
                    bots.remove(bot.entityId());
                    continue;
                }
                Player owner = plugin.getServer().getPlayer(bot.ownerId());
                if (owner != null && (!owner.getWorld().equals(bot.location().getWorld())
                        || owner.getLocation().distanceSquared(bot.location()) > 100.0D * 100.0D)) {
                    remove(bot, false);
                    continue;
                }
                bot.npc().getNavigator().cancelNavigation();
                if (bot.options().slowFalling() && bot.npc().getEntity() instanceof Player player) {
                    applySlowFalling(player);
                }
                Entity entity = bot.npc().getEntity();
                entity.setGlowing(bot.options().glowing());
                if (bot.options().shield()) {
                    bot.npc().getOrAddTrait(Equipment.class).set(EquipmentSlot.HAND, ItemFactory.shield());
                }
                if (bot.options().shield() && entity instanceof Player player && !player.isHandRaised()
                        && player.getCooldown(player.getInventory().getItemInMainHand().getType()) == 0) {
                    raiseShield(player);
                }
                if (bot.options().lookAtPlayer() && entity instanceof Player player) {
                    lookAtNearestPlayer(bot, player);
                }
            }
        }, 2L, 2L);
    }

    public void shutdown() {
        if (maintenanceTask != null) {
            maintenanceTask.cancel();
        }
        for (TrainingBot bot : bots.values().toArray(TrainingBot[]::new)) {
            remove(bot, false);
        }
        bots.clear();
    }

    private void remove(TrainingBot bot, boolean respawn) {
        bots.remove(bot.entityId());
        if (bot.npc().isSpawned()) {
            bot.npc().despawn(DespawnReason.REMOVAL);
        }
        bot.npc().destroy();
        if (respawn) {
            handleDeath(bot);
        }
    }

    private void configureSkin(NPC npc, Player owner) {
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.setSkinName(owner.getName());
    }

    private void configureEquipment(NPC npc, BotOptions options) {
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        equipment.set(EquipmentSlot.HELMET, ItemFactory.configuredArmor(configuration.section("armor.helmet"), options.helmet()));
        equipment.set(EquipmentSlot.CHESTPLATE, ItemFactory.configuredArmor(configuration.section("armor.chestplate"), options.chestplate()));
        equipment.set(EquipmentSlot.LEGGINGS, ItemFactory.configuredArmor(configuration.section("armor.leggings"), options.leggings()));
        equipment.set(EquipmentSlot.BOOTS, ItemFactory.configuredArmor(configuration.section("armor.boots"), options.boots()));
        equipment.set(EquipmentSlot.HAND, options.shield() ? ItemFactory.shield() : ItemFactory.stacked("TOTEM_OF_UNDYING", 99));
        equipment.set(EquipmentSlot.OFF_HAND, ItemFactory.stacked("TOTEM_OF_UNDYING", 99));
    }

    private void giveStarterPack(Player player) {
        if (!plugin.getConfig().getBoolean("starter-pack.enabled", true)) {
            return;
        }
        org.bukkit.configuration.ConfigurationSection items = configuration.section("starter-pack.items");
        if (items == null) {
            return;
        }
        for (String itemName : items.getKeys(false)) {
            ItemStack item = ItemFactory.simple(itemName);
            if (item == null) {
                continue;
            }
            item.setAmount(Math.min(item.getMaxStackSize(), Math.max(1, items.getInt(itemName, 1))));
            player.getInventory().addItem(item);
        }
    }

    private void applySlowFalling(Player player) {
        com.cryptomorin.xseries.XPotion.matchXPotion("SLOW_FALLING")
                .map(potion -> potion.buildPotionEffect(100, 0))
                .ifPresent(player::addPotionEffect);
    }

    private void raiseShield(Player player) {
        if (shieldUseUnavailable) {
            return;
        }
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Class<?> handClass = Class.forName("net.minecraft.world.InteractionHand");
            Object mainHand = handClass.getEnumConstants()[0];
            handle.getClass().getMethod("startUsingItem", handClass).invoke(handle, mainHand);
        } catch (ReflectiveOperationException exception) {
            shieldUseUnavailable = true;
        }
    }

    private void lookAtNearestPlayer(TrainingBot bot, Player botPlayer) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : bot.location().getWorld().getPlayers()) {
            if (getBot(player) != null) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(botPlayer.getLocation());
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        if (nearest != null) {
            Location facing = botPlayer.getEyeLocation();
            facing.setDirection(nearest.getEyeLocation().toVector().subtract(facing.toVector()));
            botPlayer.setRotation(facing.getYaw(), facing.getPitch());
        }
    }
}