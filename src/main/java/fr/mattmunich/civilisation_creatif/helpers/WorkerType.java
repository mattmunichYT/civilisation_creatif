package fr.mattmunich.civilisation_creatif.helpers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Villager;

public enum WorkerType {//                                                                                                      //UTILITÉS
    //UTILITAIRES/COPETENCES *** IMMORTELS (=-1)
    PILOTE_AVION(3500,3000,-1,Material.DIAMOND_HELMET,ChatColor.GREEN, Villager.Profession.CARTOGRAPHER), //AEROPTORT
    PILOTE_TRAIN(3500,3000,-1,Material.RAIL,ChatColor.GREEN, Villager.Profession.CARTOGRAPHER),           //TRAIN
    NAVIGATEUR(3500,3000,-1,Material.OAK_BOAT,ChatColor.GREEN, Villager.Profession.CARTOGRAPHER),         //PORT
    //BONUS JOUEUR
    INGENIEUR(500,3000,120,Material.CRAFTER,ChatColor.GREEN, Villager.Profession.LIBRARIAN),          //VOYAGES PRIX REDUITS
    JARDINIER(500,1000,120,Material.CRAFTER,ChatColor.GREEN, Villager.Profession.LIBRARIAN),          //+ARGENT QUAND BLOCK NATUREL(=/= bois) PLACE
    BUCHERON(500,1000,45,Material.SPRUCE_LOG,ChatColor.GOLD, Villager.Profession.LIBRARIAN),           //+ARGENT QUAND BLOCK BOIS PLACE
    //ALIMENTATION VILLAGEOIS => + BIEN-ÊTRE => + ARGENT
    BOULANGER(1000,500,90,Material.BREAD,ChatColor.GOLD, Villager.Profession.FARMER),
    BOUCHER(1700,1000,90,Material.SMOKER,ChatColor.GOLD, Villager.Profession.BUTCHER),
    POISSONNIER(1700,1700,90,Material.TROPICAL_FISH,ChatColor.GOLD, Villager.Profession.FISHERMAN),
    PECHEUR(1300,500,60,Material.FISHING_ROD,ChatColor.GOLD, Villager.Profession.FISHERMAN),
    AGRICULTEUR(2000,2000,60,Material.COMPOSTER,ChatColor.YELLOW, Villager.Profession.FARMER),
    ELEVEUR(2000,2000,60,Material.WHITE_WOOL,ChatColor.YELLOW, Villager.Profession.SHEPHERD),
    //SECURITE VILLAGEOIS => + BIEN-ÊTRE => + ARGENT
    INFIRMIER(1800,1000,60,Material.ENCHANTED_GOLDEN_APPLE,ChatColor.GOLD, Villager.Profession.CLERIC),
    MEDECIN(4300,4000,150,Material.BREWING_STAND,ChatColor.DARK_BLUE, Villager.Profession.CLERIC),
    POMPIER(1700,1000,60,Material.CAMPFIRE,ChatColor.GOLD, Villager.Profession.LEATHERWORKER),
    SOLDAT(2500,2000,90,Material.IRON_SWORD,ChatColor.YELLOW, Villager.Profession.WEAPONSMITH),
    POLICIER(1400,1000,120,Material.SHIELD,ChatColor.GOLD, Villager.Profession.TOOLSMITH);

    private final Material item;
    private final ChatColor color;
    private final Villager.Profession profession;
    private final int income,price,lifespan;

    WorkerType(int income, int price, int lifespan /*in days*/, Material item, ChatColor color, Villager.Profession profession) {
        this.income = income;
        this.price = price;
        this.lifespan = lifespan;
        this.item = item;
        this.color = color;
        this.profession = profession;
    }

    public int getIncome() { return income; }
    public int getPrice() { return price; }
    public int getLifespan() { return lifespan; }
    public Material getItem() { return item; }
    public ChatColor getColor() { return color; }
    public Villager.Profession getProfession() {return profession;}
}
