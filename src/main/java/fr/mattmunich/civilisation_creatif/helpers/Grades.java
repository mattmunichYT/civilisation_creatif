package fr.mattmunich.civilisation_creatif.helpers;

import fr.mattmunich.civilisation_creatif.Main;

public enum Grades {
    // Grades
    // rgb.birdflop.com - Presets :

    ADMIN(10, "§l#fb0000§lA#fc2727§lD#fc4e4e§lM#fd7474§lI#fd9b9b§lN §6§l", "§6", " §8§l>>§6§l "),

    MODO(9, "#fb8f00§lM#fca42b§lO#fcba55§lD#fdcf80§lO §6§l", "§6", " §8§l>>§e "),

    DEV(8, "#696969§lD#878787§lE#A5A5A5§lV §7§l", "§8", " §8§l>>§7 "),

    BUILDEUR(7, "#fb00f8B#f812f9U#f524f9I#f236faL#f047fbD#ed59fcE#ea6bfcU#e77dfdR §5", "§6", " §8>>§2 "),

    ANIMATEUR(6, "#0d00fbA#2116fbN#352dfcI#4943fcM#5d5afcA#7170fcT#8586fdE#999dfdU#adb3fdR §b", "§b", " §8>>§b "),

    GUIDE(5, "#fbcb00G#fcd62bU#fce156I#fdec80D#fdf7abE §e", "§6", "§8§l>> §a "),

    VIDEASTE(4, "#b304fbV#ba18fbI#c12cfcD#c840fcE#cf53fcA#d667fcS#dd7bfdT#e48ffdE §5", "§5", " §8>>§d "),

    VIP(3, "#ffcf00V#ffe55fI#fffabeP §a", "§a", " §8>>§a "),

    TESTEUR(2, "#fbbc00T#e0c70dE#c4d21aS#a9dd27T#8de733E#72f240U#56fd4dR §6", "§6", " §2>>§6 "),

    MEMBRE(1, "#b1b1b1M#c1c1c1E#d0d0d0M#e0e0e0B#efefefR#ffffffE §7", "§1", " §l§8>>§3 ");

    // end("Grades")

    // Fields

    private final int id;
    private final String prefix, suffix, chatSeparator;

    // end("Fields")

    // Constreucteur
    private Grades(int id, String prefix, String suffix, String chatSeparator) {
        this.id = id;
        this.prefix = prefix;
        this.suffix = suffix;
        this.chatSeparator = chatSeparator;

    }
    // end("Constructeur")

    // Methode GETTER

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getName() {
        return this.toString();
    }

    public String getChatSeparator() {
        return chatSeparator;
    }

    public int getId() {
        return id;
    }

    // end("Methode GETTER")


}
