package fr.mattmunich.civilisation_creatif.helpers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Villager;

public enum WorkerType {//                                                                                                      //UTILITÉS
    //UTILITAIRES/COPETENCES *** IMMORTELS (=-1)
    PILOTE_AVION(3500,3000,-1,Material.DIAMOND_HELMET,ChatColor.DARK_RED, Villager.Profession.CARTOGRAPHER,Sound.ENTITY_BREEZE_IDLE_GROUND,0), //AEROPTORT
    PILOTE_TRAIN(3500,3000,-1,Material.RAIL,ChatColor.DARK_RED, Villager.Profession.CARTOGRAPHER,Sound.ENTITY_MINECART_RIDING,1.5f),           //TRAIN
    NAVIGATEUR(3500,3000,-1,Material.OAK_BOAT,ChatColor.DARK_RED, Villager.Profession.CARTOGRAPHER,Sound.ENTITY_DOLPHIN_JUMP,1),         //PORT
    //BONUS JOUEUR
    INGENIEUR(1000,3000,120,Material.CRAFTER,ChatColor.DARK_PURPLE, Villager.Profession.LIBRARIAN,Sound.BLOCK_PISTON_CONTRACT,1),          //VOYAGES PRIX REDUITS
    JARDINIER(750,1000,60,Material.FLOWERING_AZALEA,ChatColor.DARK_PURPLE, Villager.Profession.LIBRARIAN,Sound.BLOCK_GRASS_BREAK,1),          //+ARGENT QUAND BLOCK NATUREL(=/= bois) PLACE
    BUCHERON(750,1000,60,Material.SPRUCE_LOG,ChatColor.DARK_PURPLE, Villager.Profession.LIBRARIAN,Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,1),           //+ARGENT QUAND BLOCK BOIS PLACE
    //ALIMENTATION VILLAGEOIS => + BIEN-ÊTRE => + ARGENT
    BOULANGER(2000,2000,90,Material.BREAD,ChatColor.YELLOW, Villager.Profession.FARMER,Sound.ENTITY_GENERIC_EAT,1),
    BOUCHER(1200,1000,60,Material.SMOKER,ChatColor.GOLD, Villager.Profession.BUTCHER,Sound.ENTITY_VILLAGER_WORK_BUTCHER,1),
    POISSONNIER(2000,2000,90,Material.TROPICAL_FISH,ChatColor.YELLOW, Villager.Profession.FISHERMAN,Sound.ENTITY_SALMON_DEATH,1),
    PECHEUR(1300,1000,60,Material.FISHING_ROD,ChatColor.GOLD, Villager.Profession.FISHERMAN,Sound.ENTITY_FISHING_BOBBER_SPLASH,1),
    AGRICULTEUR(1100,1000,60,Material.COMPOSTER,ChatColor.GOLD, Villager.Profession.FARMER,Sound.ITEM_BONE_MEAL_USE,1),
    ELEVEUR(1600,1000,60,Material.WHITE_WOOL,ChatColor.GOLD, Villager.Profession.SHEPHERD,Sound.ENTITY_SHEEP_HURT,1),
    //SECURITE VILLAGEOIS => + BIEN-ÊTRE => + ARGENT
    INFIRMIER(3000,3000,120,Material.ENCHANTED_GOLDEN_APPLE,ChatColor.GREEN, Villager.Profession.CLERIC,Sound.ENTITY_WARDEN_HEARTBEAT,1),
    MEDECIN(4300,4000,150,Material.BREWING_STAND,ChatColor.DARK_BLUE, Villager.Profession.CLERIC,Sound.BLOCK_BREWING_STAND_BREW,1),
    POMPIER(1700,1000,60,Material.CAMPFIRE,ChatColor.GOLD, Villager.Profession.LEATHERWORKER,Sound.BLOCK_CAMPFIRE_CRACKLE,1),
    SOLDAT(2500,2000,90,Material.IRON_SWORD,ChatColor.YELLOW, Villager.Profession.WEAPONSMITH,Sound.ENTITY_DRAGON_FIREBALL_EXPLODE,1),
    POLICIER(1400,3000,60,Material.SHIELD,ChatColor.GOLD, Villager.Profession.TOOLSMITH,Sound.ITEM_ARMOR_EQUIP_NETHERITE,1);

    private final Material item;
    private final ChatColor color;
    private final Villager.Profession profession;
    private final int income,price,lifespan;
    private final Sound sound;
    private final float soundPitch;

    WorkerType(int income, int price, int lifespan /*in days*/, Material item, ChatColor color, Villager.Profession profession, Sound sound, float soundPitch) {
        this.income = income;
        this.price = price;
        this.lifespan = lifespan;
        this.item = item;
        this.color = color;
        this.profession = profession;
        this.sound = sound;
        this.soundPitch = soundPitch;
    }

    public int getIncome() { return income; }
    public int getPrice() { return price; }
    public int getLifespan() { return lifespan; }
    public Material getItem() { return item; }
    public ChatColor getColor() { return color; }
    public Villager.Profession getProfession() {return profession;}
    public Sound getSound() {return sound;}
    public float getSoundPitch() {return soundPitch;}
}
