package fr.mattmunich.civilisation_creatif.helpers;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class Backup {
    private final Plugin plugin;

    private final Main main;

    public Backup(Plugin plugin, Main main) {
        this.plugin = plugin;
        this.main = main;
    }

    public BukkitTask backupTask = null;

    private final DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-HH:mm");

    public void run() {
        createBackups();
        deleteOldBackups(4);
        scheduleNextBackup();
    }

    private void createBackups(){
        //Creating backups
        Bukkit.getConsoleSender().sendMessage(main.prefix + "§eBackups des mondes en cours...");
        for(World world : Bukkit.getWorlds()) {
            try {
                File f = new File("Backups/" + formatter.format(System.currentTimeMillis()) + "/" + world.getName() + "/");
                try {
                    boolean check = f.mkdirs();
                    if(check) {
                        Bukkit.getConsoleSender().sendMessage(main.prefix + "§2Création du backup pour le monde §6" + world.getName() + "§2...");
                    }
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Un backup a déjà été créé récemment !");
                    return;
                }

                File origin = world.getWorldFolder();
                if(!origin.exists()) {
                    Bukkit.getConsoleSender().sendMessage(main.prefix + "§4§lUne erreur s'est produite lors de la sauvegarde du monde §c ! - L'origine n'existe pas");
                }
                if(!f.exists()) {
                    Bukkit.getConsoleSender().sendMessage(main.prefix + "§4§lUne erreur s'est produite lors de la sauvegarde du monde §c ! - La destination n'existe pas");
                }
                org.apache.commons.io.FileUtils.copyDirectory(origin, f);//main thing (basically copies the world files)

                Bukkit.getConsoleSender().sendMessage(main.prefix + "§2Le monde §a" + world.getName() + "§2 a été sauvegardé !");
            } catch (Exception e) {
                main.logError("Couldn't backup world " + world.getName(),e);
            }
        }
    }

    private void deleteOldBackups(int daysKept){
        // Deleting old backups
        try {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§eSuppression des anciens backups...");
            File backupDir = new File("Backups/");
            File[] backups = backupDir.listFiles();
            if (backups != null) {
                for (File backup : backups) {
                    if (backup.isFile() || backup.isDirectory()) {
                        String backupName = backup.getName();
                        long backupMillis = formatter.parse(backupName).getTime();
                        long difference = System.currentTimeMillis() - backupMillis;

                        if (difference > daysKept* 86400000L) {
                            try {
                                org.apache.commons.io.FileUtils.forceDelete(backup); //main thing
                            } catch (IOException deleteEx) {
                                main.logError("§4Failed to delete backup " + backupName,deleteEx);
                            }
                        }
                    }
                }
            }
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§aLes backups vieux d'au moins " + daysKept + " jours ont été supprimés !");
        } catch (Exception e) {
            main.logError("§4Failed to delete old backup ",e);
        }
    }

    public void scheduleNextBackup(){
        if(backupTask != null && !backupTask.isCancelled()) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "Duplicate BackupTask detected! Cancelling previous one.");
            backupTask.cancel();
        }

        //Schedule next backup
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        if(cal.get(Calendar.HOUR_OF_DAY) >= 22)
            cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long offset = cal.getTimeInMillis() - now;
        long ticks = offset / 50L;
        try {
            backupTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                this.run();
                backupTask = null;
            }, ticks);
        } catch (Exception e) {
            main.logError("Coulnd't schedule backup",e);
        }
    }
}