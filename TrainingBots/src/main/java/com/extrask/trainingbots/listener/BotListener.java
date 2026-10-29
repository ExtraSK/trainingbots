package com.extrask.trainingbots.listener;

import com.extrask.trainingbots.bot.BotManager;
import com.extrask.trainingbots.bot.TrainingBot;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class BotListener implements Listener {
    private final BotManager botManager;

    public BotListener(BotManager botManager) {
        this.botManager = botManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        TrainingBot bot = botManager.getBot(event.getEntity());
        if (bot == null && event.getEntity() instanceof org.bukkit.entity.Player player && botManager.shouldProtectOwner(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        TrainingBot bot = botManager.getBot(entity);
        if (bot != null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            botManager.handleDeath(bot);
        }
    }
}