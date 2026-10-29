package com.extrask.trainingbots.util;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemFactory {
    private ItemFactory() {
    }

    public static ItemStack simple(String materialName) {
        return XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
    }

    public static ItemStack stacked(String materialName, int amount) {
        ItemStack item = simple(materialName);
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setMaxStackSize(amount);
            item.setItemMeta(meta);
        }
        item.setAmount(amount);
        return item;
    }

    public static ItemStack shield() {
        ItemStack item = simple("SHIELD");
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            XEnchantment.matchXEnchantment("UNBREAKING").ifPresent(enchantment ->
                    meta.addEnchant(enchantment.getEnchant(), 3, true));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack configuredArmor(ConfigurationSection section, boolean blastProtection) {
        if (section == null) {
            return null;
        }
        ItemStack item = simple(section.getString("material", "AIR"));
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        String enchantmentName = blastProtection ? "BLAST_PROTECTION" : "PROTECTION";
        XEnchantment.matchXEnchantment(enchantmentName).ifPresent(xEnchantment ->
                meta.addEnchant(xEnchantment.getEnchant(), 4, true));
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}