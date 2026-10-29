package com.extrask.trainingbots.bot;

import com.extrask.trainingbots.menu.BotOptions;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

import java.util.UUID;

public record TrainingBot(UUID entityId, UUID ownerId, Location location, BotOptions options, NPC npc) {
}