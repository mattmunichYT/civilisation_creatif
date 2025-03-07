package fr.mattmunich.civilisation_creatif.helpers;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionChecker {

    private Main main;

    public VersionChecker(Main main) {
        this.main = main;
    }

    public double getVersion() {
        String regex = "\\d+\\.\\d+(\\.\\d+)?";

        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);

        // Find a match in the input string
        Matcher matcher = pattern.matcher(Bukkit.getVersion());
        if (matcher.find()) {
            String version = matcher.group(0); // Get the matched version number
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§aDetected server version §2" + version);
            try {
                return Double.parseDouble(version.replaceFirst("1.",""));
            } catch (NumberFormatException e) {
                Bukkit.getConsoleSender().sendMessage("§c--------------------------------------------------------------------");
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't get server version !");
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Some features might not work.");
                Bukkit.getConsoleSender().sendMessage("§e------------------------------------------");
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4La version du server n'a pas pu être obtenue !");
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Certaines fonctionnalitées pourraient ne pas fonctionner.");
                Bukkit.getConsoleSender().sendMessage("§c--------------------------------------------------------------------");
                return 0;
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("§c--------------------------------------------------------------------");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't get server version !");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Some features might not work.");
            Bukkit.getConsoleSender().sendMessage("§e------------------------------------------");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4La version du server n'a pas pu être obtenue !");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Certaines fonctionnalitées pourraient ne pas fonctionner.");
            Bukkit.getConsoleSender().sendMessage("§c--------------------------------------------------------------------");
            return 0;
        }
    }
}

