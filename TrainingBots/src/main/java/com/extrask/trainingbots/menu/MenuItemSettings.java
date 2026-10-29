package com.extrask.trainingbots.menu;

import java.util.List;

public record MenuItemSettings(int slot, String material, String name, List<String> lore) {
    public MenuItemSettings {
        lore = List.copyOf(lore);
    }
}