package com.extrask.trainingbots.config;

import java.util.List;

public record BotSettings(
        String name,
        String skinPlayerName,
        String skinTexture,
        String skinSignature,
        boolean slowFalling,
        boolean immortality,
        boolean respawnAfterDeath,
        long respawnDelay,
        double spawnRadius,
        double immortalityRadius,
        boolean doubleBlastProtection,
        boolean shield,
        boolean glowing,
        boolean lookAtPlayer,
        double existingBotRadius,
        List<String> disabledWorlds
) {
}