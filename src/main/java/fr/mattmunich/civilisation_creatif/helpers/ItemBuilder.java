package fr.mattmunich.civilisation_creatif.helpers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    public static ItemStack getItem(Material material, String customName, boolean Enchanted, boolean Unbreakable, String DescLine1, String DescLine2, String DescLine3) {

        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemM = item.getItemMeta();

        if(customName != null) {
            itemM.setDisplayName(customName);
        } else {
            itemM.setDisplayName("");
        }
        if(Enchanted) {
            itemM.addEnchant(Enchantment.PROTECTION, 1, Enchanted);
        }
        if(Unbreakable) {
            itemM.setUnbreakable(Unbreakable);
        }
        if(DescLine1 != null && DescLine2 != null && DescLine3 != null) {
            itemM.setLore(Arrays.asList(DescLine1, DescLine2, DescLine3));
        }
        itemM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemM.addItemFlags(ItemFlag.HIDE_DESTROYS);
        itemM.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);


        item.setItemMeta(itemM);
        return item;

    }

    public static ItemStack getItem(Material material, String customName, boolean Enchanted, boolean Unbreakable, String DescLine1, String DescLine2, String DescLine3,String DescLine4) {

        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemM = item.getItemMeta();

        if(customName != null) {
            itemM.setDisplayName(customName);
        } else {
            itemM.setDisplayName("");
        }
        if(Enchanted) {
            itemM.addEnchant(Enchantment.PROTECTION, 1, Enchanted);
        }
        if(Unbreakable) {
            itemM.setUnbreakable(Unbreakable);
        }
        if(DescLine1 != null && DescLine2 != null && DescLine3 != null && DescLine4 !=null) {
            itemM.setLore(Arrays.asList(DescLine1, DescLine2, DescLine3,DescLine4));
        }
        itemM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemM.addItemFlags(ItemFlag.HIDE_DESTROYS);
        itemM.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);


        item.setItemMeta(itemM);
        return item;

    }

    public static ItemStack getItem(Material material, String customName) {

        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemM = item.getItemMeta();

        if(customName != null) {
            itemM.setDisplayName(customName);
        } else {
            itemM.setDisplayName("");
        }


        item.setItemMeta(itemM);
        return item;

    }
    public static ItemStack getItem(Material material, String customName, List<String> lore) {

        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemM = item.getItemMeta();

        if(customName != null) {
            itemM.setDisplayName(customName);
        } else {
            itemM.setDisplayName("");
        }
        itemM.setLore(lore);


        item.setItemMeta(itemM);
        return item;

    }
}
