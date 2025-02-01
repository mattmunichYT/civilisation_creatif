package fr.mattmunich.civilisation_creatif.helpers;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.entity.Player;

public enum Grades {
    // Grades
    // rgb.birdflop.com - Presets :

    ADMIN(5, "§l#fb0000§lA#fc2727§lD#fc4e4e§lM#fd7474§lI#fd9b9b§lN §6§l", "§6", " §8§l>>§6§l "),

    MODO(4, "#fb8f00§lM#fca42b§lO#fcba55§lD#fdcf80§lO §6§l", "§6", " §8§l>>§e "),

    CHEF(3, "#00CF41§lC#00AD2B§lH#018B16§lE#016900§lF §2§l", "§a", " §l§8>>§a "),

    MEMBRE(2, "#00CF41M#00BB34E#00A627M#01921AB#017D0DR#016900E §2", "§a", " §8>>§a "),

    VAGABOND(1, "#D4D4D4V#CBCBCBA#C3C3C3G#BABABAA#B2B2B2B#A9A9A9O#A1A1A1N#989898D §7", "§f", " §8>>§f"),

    JUGE_BUILD(0, "#BEFF00J#C5F400U#CBE900G#D2DE00E #DFC800B#E5BD00U#ECB200I#F2A700L#F99C00D#FF9100S §e§l", "§f", " §8>>§e");

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

    public static Grades getGradeById(int id) {
        for(Grades grade : values()) {
            if(grade.getId() == id) {
                return grade;
            }
        }

        return Grades.MEMBRE;
    }

    public static boolean isInferior(Player p, int id) {
        PlayerData data = null;
        try {
            data = new PlayerData(p.getUniqueId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int pId = data.getRank().getId();
        return pId < id;
    }

    public static boolean isSuperior(Player p, int id) {
        PlayerData data = null;
        try {
            data = new PlayerData(p.getUniqueId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int pId = data.getRank().getId();
        return pId > id;
    }

    // end("Methode GETTER")


}
