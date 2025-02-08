package fr.mattmunich.civilisation_creatif.helpers;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum WorkerType {
    ARCHEOLOGUE(1300,1000,60,Material.IRON_SHOVEL,ChatColor.GOLD),
    PILOTE(3500,3000,60,Material.LEATHER_HELMET,ChatColor.GREEN),
    GARAGISTE(1600,1000,60,Material.REDSTONE_TORCH,ChatColor.GOLD),
    FLEURISTE(1500,1000,60,Material.FLOWERING_AZALEA,ChatColor.GOLD),
    PSYCHOLOGUE(3000,3000,60,Material.TOTEM_OF_UNDYING,ChatColor.GREEN),
    BOULANGER(1000,500,60,Material.BREAD,ChatColor.RED),
    PRIMEUR(1300,500,60,Material.APPLE,ChatColor.RED),
    POLITICIEN(1400,1000,60,Material.GOLDEN_SWORD,ChatColor.GOLD),
    BANQUIER(2200,2000,60,Material.EMERALD,ChatColor.YELLOW),
    BUCHERON(1300,500,60,Material.SPRUCE_LOG,ChatColor.RED),
    INFIRMIER(1800,1000,60,Material.ENCHANTED_GOLDEN_APPLE,ChatColor.GOLD),
    AVOCAT(3700,3000,60,Material.BLACK_BANNER,ChatColor.GREEN),
    JARDINIER(1200,500,60,Material.RED_TULIP,ChatColor.RED),
    PROFESSEUR(1000,1000,60,Material.WRITABLE_BOOK,ChatColor.YELLOW),
    JOURNALISTE(3800,3000,60,Material.PAPER,ChatColor.GREEN),
    PHOTOGRAPHE(1500,1000,60,Material.PAINTING,ChatColor.GOLD),
    ARCHITECHTE(1700,1000,60,Material.CRAFTING_TABLE,ChatColor.GOLD),
    CUISINIER(2100,2000,60,Material.CAKE,ChatColor.YELLOW),
    AGRICULTEUR(2000,2000,60,Material.COMPOSTER,ChatColor.YELLOW),
    MEDECIN(4300,4000,60,Material.BREWING_STAND,ChatColor.DARK_BLUE),
    //DEFINE PRICES!
    POMPIER(0,0,60,Material.CAMPFIRE,ChatColor.WHITE),
    SOLDAT(0,0,60,Material.IRON_SWORD,ChatColor.WHITE),
    POISSONNIER(0,0,60,Material.TROPICAL_FISH,ChatColor.WHITE),
    STYLISTE(0,0,60,Material.ARMOR_STAND,ChatColor.WHITE),
    VETERINAIRE(0,0,60,Material.TURTLE_EGG,ChatColor.WHITE),
    POLICIER(0,0,60,Material.SHIELD,ChatColor.WHITE),
    INGENIEUR(0,0,60,Material.CRAFTER,ChatColor.WHITE),
    CHARPENTIER(0,0,60,Material.BRICKS,ChatColor.WHITE),
    PLOMBIER(0,0,60,Material.WATER_BUCKET,ChatColor.WHITE),
    BOUCHER(0,0,60,Material.SMOKER,ChatColor.WHITE);

    WorkerType(int price, int income, int lifespan /*in days*/, Material item, ChatColor color) {}
}
