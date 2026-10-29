package com.extrask.trainingbots.menu;

import com.extrask.trainingbots.bot.BotManager;
import com.extrask.trainingbots.config.BotConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BotMenuListener implements Listener {
    private final BotManager botManager;
    private final BotConfiguration configuration;
    private final Map<UUID, BotMenu> menus = new HashMap<>();

    public BotMenuListener(BotManager botManager, BotConfiguration configuration) {
        this.botManager = botManager;
        this.configuration = configuration;
    }

    public void open(Player player) {
        BotMenu menu = new BotMenu(player, configuration.options(player.getUniqueId()), configuration);
        menus.put(player.getUniqueId(), menu);
        menu.open();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        BotMenu menu = menus.get(player.getUniqueId());
        if (menu == null || event.getInventory() != menu.inventory()) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (menu.isAtSlot("helmet", slot)) {
            menu.toggleHelmet();
        } else if (menu.isAtSlot("slow-falling", slot)) {
            menu.toggleSlowFalling();
        } else if (menu.isAtSlot("immortality", slot)) {
            menu.toggleImmortality();
        } else if (menu.isAtSlot("shield", slot)) {
            menu.toggleShield();
        } else if (menu.isAtSlot("chestplate", slot)) {
            menu.toggleChestplate();
        } else if (menu.isAtSlot("glowing", slot)) {
            menu.toggleGlowing();
        } else if (menu.isAtSlot("look-at-player", slot)) {
            menu.toggleLookAtPlayer();
        } else if (menu.isAtSlot("leggings", slot)) {
            menu.toggleLeggings();
        } else if (menu.isAtSlot("boots", slot)) {
            menu.toggleBoots();
        } else if (menu.isAtSlot("spawn", slot)) {
            spawnFromMenu(player, menu);
        }
        configuration.saveOptions(player.getUniqueId(), menu.options());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        BotMenu menu = menus.get(event.getPlayer().getUniqueId());
        if (menu != null && event.getInventory() == menu.inventory()) {
            menus.remove(event.getPlayer().getUniqueId());
        }
    }

    private void spawnFromMenu(Player player, BotMenu menu) {
        if (botManager.isSpawnWorldDisabled(player)) {
            player.sendMessage(configuration.message("spawn-world-disabled", "&cYou can not spawn a bot in Spawn."));
            return;
        }
        if (botManager.hasNearbyOwnedBot(player, configuration.settings().existingBotRadius())) {
            player.sendMessage(configuration.message("bot-too-close", "&cThere is already a bot close."));
            return;
        }
        if (botManager.hasOtherPlayerNearby(player.getLocation(), player, configuration.settings().spawnRadius())) {
            player.sendMessage(configuration.message("player-too-close").replace("{radius}", String.valueOf((int) configuration.settings().spawnRadius())));
            return;
        }
        if (botManager.spawn(player, player.getLocation(), menu.options()) != null) {
            player.sendMessage(configuration.message("bot-spawned"));
            player.closeInventory();
        }
    }
}