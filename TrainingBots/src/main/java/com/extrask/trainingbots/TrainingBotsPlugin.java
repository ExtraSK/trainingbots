package com.extrask.trainingbots;

import com.extrask.trainingbots.bot.BotManager;
import com.extrask.trainingbots.command.BotCommand;
import com.extrask.trainingbots.config.BotConfiguration;
import com.extrask.trainingbots.listener.BotListener;
import com.extrask.trainingbots.menu.BotMenuListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrainingBotsPlugin extends JavaPlugin {
    private BotConfiguration configuration;
    private BotManager botManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configuration = new BotConfiguration(this);
        configuration.reload();
        botManager = new BotManager(this, configuration);

        PluginCommand command = getCommand("bot");
        if (command == null) {
            throw new IllegalStateException("The bot command is missing from plugin.yml");
        }
        BotCommand botCommand = new BotCommand(botManager, configuration);
        command.setExecutor(botCommand);
        command.setTabCompleter(botCommand);

        getServer().getPluginManager().registerEvents(new BotListener(botManager), this);
        getServer().getPluginManager().registerEvents(new BotMenuListener(botManager, configuration), this);
        botManager.startMaintenance();
    }

    @Override
    public void onDisable() {
        if (botManager != null) {
            botManager.shutdown();
        }
    }
}