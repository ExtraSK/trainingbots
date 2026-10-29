package com.extrask.trainingbots.command;

import com.extrask.trainingbots.bot.BotManager;
import com.extrask.trainingbots.config.BotConfiguration;
import com.extrask.trainingbots.menu.BotMenuListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class BotCommand implements CommandExecutor, TabCompleter {
    private final BotManager botManager;
    private final BotConfiguration configuration;

    public BotCommand(BotManager botManager, BotConfiguration configuration) {
        this.botManager = botManager;
        this.configuration = configuration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("trainingbots.use")) {
            player.sendMessage(configuration.message("no-permission"));
            return true;
        }
        if (args.length == 0) {
            if (botManager.removeNearbyOwnedBot(player, 100.0D)) {
                player.sendMessage(configuration.message("bot-removed"));
                return true;
            }
            openMenu(player);
            return true;
        }
        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "spawn" -> spawn(player);
            case "remove" -> botManager.removeNearest(player);
            case "reload" -> reload(player);
            case "help" -> player.sendMessage(configuration.message("help"));
            default -> player.sendMessage(configuration.message("help"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? List.of("spawn", "remove", "reload", "help") : List.of();
    }

    private void openMenu(Player player) {
        if (botManager.isSpawnWorldDisabled(player)) {
            player.sendMessage(configuration.message("spawn-world-disabled", "&cYou can not spawn a bot in Spawn."));
            return;
        }
        Plugin plugin = player.getServer().getPluginManager().getPlugin("TrainingBots");
        if (plugin == null) {
            return;
        }
        for (org.bukkit.event.Listener listener : org.bukkit.event.HandlerList.getRegisteredListeners(plugin).stream()
                .map(org.bukkit.plugin.RegisteredListener::getListener).toList()) {
            if (listener instanceof BotMenuListener menuListener) {
                menuListener.open(player);
                return;
            }
        }
    }

    private void spawn(Player player) {
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
        if (botManager.spawn(player, player.getLocation(), configuration.options(player.getUniqueId())) != null) {
            player.sendMessage(configuration.message("bot-spawned"));
        }
    }

    private void reload(Player player) {
        if (!player.hasPermission("trainingbots.admin")) {
            player.sendMessage(configuration.message("no-permission"));
            return;
        }
        configuration.reload();
        player.sendMessage(configuration.message("reloaded"));
    }
}