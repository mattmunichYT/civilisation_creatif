package fr.mattmunich.civilisation_creatif.helpers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Backup {
    private final Plugin plugin;

    public Backup(Plugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-HH:mm");
        //Creating backups
        Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §eBackups des mondes en cours...");
        for(World world : Bukkit.getWorlds()) {
            try {
                File f = new File("Backups/" + formatter.format(System.currentTimeMillis()) + "/");
                try {
                    boolean check = f.mkdirs();
                    if(check) {
                        Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §2Le système de backups a été configuré !");
                    }
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §4Un backup a déjà été créé récemment !");
                    return;
                }

                File origin = world.getWorldFolder();
                if(!origin.exists()) {
                    Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §4§lUne erreur s'est produite lors de la sauvegarde du monde §c ! - L'origine n'existe pas");
                }
                if(!f.exists()) {
                    Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §4§lUne erreur s'est produite lors de la sauvegarde du monde §c ! - La destination n'existe pas");
                }
                org.apache.commons.io.FileUtils.copyDirectory(origin, f);//main thing

                Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §2Le monde §a" + world.getName() + "§2 a été sauvegardé !");
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §4§lUne erreur s'est produite lors de la sauvegarde du monde §c" + world.getName() + " §4§l! Erreur ci-dessous.");
                Bukkit.getConsoleSender().sendMessage("§4" + Arrays.toString(e.getStackTrace()));
            }
        }
        // Deleting old backups
        try {
            Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §eSuppression des anciens backups...");
            File backupDir = new File("Backups/");
            File[] backups = backupDir.listFiles();

            if (backups != null) {
                for (File backup : backups) {
                    if (backup.isFile() || backup.isDirectory()) {
                        String backupName = backup.getName();
                        long backupMillis = formatter.parse(backupName).getTime();
                        long difference = System.currentTimeMillis() - backupMillis;
                        //432000000 = 5 days in ms
                        if (difference > 432000000) {
                            try {
                                org.apache.commons.io.FileUtils.forceDelete(backup); //main thing
//                                Bukkit.getConsoleSender().sendMessage("§eBackup name : §6" + backupName + "§e ; difference :§6" + difference + "§e ; deleted : §2" + "YES");
                            } catch (IOException deleteEx) {
                                Bukkit.getConsoleSender().sendMessage("§4Failed to delete backup: " + backupName);
                                deleteEx.printStackTrace();
                            }
                        } //else {
////                            Bukkit.getConsoleSender().sendMessage("§eBackup name : §6" + backupName + "§e ; difference :§6" + difference + "§e ; deleted : §4" + "NO");
//                        }
                    }
                }
            }
            Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §aLes backups vieux d'au moins 5 jours ont été supprimés !");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§6[AdminCmdsB] §4§lUne erreur s'est produite lors de la suppression des anciens backups ! Voir l'erreur ci-dessous.");
            e.printStackTrace();
        }
    }
}