package fr.mattmunich.civilisation_creatif.listeners;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.Grades;
import fr.mattmunich.civilisation_creatif.helpers.PlayerData;
import fr.mattmunich.civilisation_creatif.helpers.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.Objects;

public class JoinListener implements Listener {

    private static Main main;
    private static TerritoryData territoryData;

    public JoinListener(Main main, TerritoryData territoryData) {
        this.main = main;
        this.territoryData = territoryData;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        PlayerData data = null;
        try {
            data = new PlayerData(p.getUniqueId());
        } catch (Exception err) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Coudln't get PlayerData of player §c" + p.getName());
        }

        if(data != null) {

            Grades pGrade = data.getRank();

            try {
                territoryData.setPlayerTerritoryTeam(p);
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Coulnd't add player to his territory team because of §r" + ex.getMessage() + Arrays.toString(ex.getStackTrace()).replace(",", ",\n"));
            }

            if(pGrade == null) {
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Impossible de trouver le grade de §c" + p.getName() + "§4 !");
            } else {
                String gradePrefix = main.hex(pGrade.getPrefix());
                int gradeID = pGrade.getId();
                String gradeSuffix = pGrade.getSuffix();
                String name = p.getName();
                if(territoryData.getTerritoryTeamOfPlayer(p) != null) {
                    name = gradePrefix + p.getName() + " §8| §7" + territoryData.getTerritoryTeamOfPlayer(p).getName();
                } else {
                    name = gradePrefix + p.getName();
                }
                p.setDisplayName(name);
                p.setPlayerListName(name);
                p.setCustomName(name);



                //(gradeID 1 == membre !)
                if(gradeID >= 2) {
                    main.testeur.add(p);
                }
                if(gradeID >= 3) {
                    main.vip.add(p);
                }
                if(gradeID >= 4) {
                    main.videaste.add(p);
                }
                if(gradeID >= 5) {
                    main.guide.add(p);
                }
                if(gradeID >= 6) {
                    main.animateur.add(p);
                }
                if(gradeID >= 7) {
                    main.buildeur.add(p);
                }
                if(gradeID >= 8) {
                    main.dev.add(p);
                }
                if(gradeID >= 9) {
                    main.modo.add(p);
                }
                if(gradeID == 10) {
                    main.admin.add(p);
                }
            }
        }

        if(!main.buildeur.contains(p)) {
            p.kickPlayer("§cAccès au serveur refusé !\n§eLe serveur est en développement.");
            return;
        }

        e.setJoinMessage("§7[§a+§7] §e" + p.getDisplayName());

        p.sendMessage("\n\n\n§e----------------------------------");
        p.sendMessage("§2        Bienvenue sur le serveur        ");
        p.sendMessage("          §2§lCivilisation §6Créatif          \n");
        p.sendMessage("§e              -----------             \n");
        p.sendMessage("§a              Propulsé par              ");
        p.sendMessage("        §x§f§f§0§0§0§0§lM§x§f§e§0§f§0§0§li§x§f§c§1§e§0§0§ln§x§f§b§2§d§0§0§li §x§f§9§3§c§0§0§lJ§x§f§8§4§b§0§0§le§x§f§7§5§a§0§0§lu§x§f§5§6§9§0§0§lx §x§f§4§7§8§0§0§lE§x§f§2§8§7§0§0§ln§x§f§1§9§6§0§0§lt§x§e§f§a§5§0§0§lr§x§e§e§b§4§0§0§le §x§e§d§c§3§0§0§lP§x§e§b§d§2§0§0§lo§x§e§a§e§1§0§0§lt§x§e§8§f§0§0§0§le§x§e§7§f§f§0§0§ls          ");
        p.sendMessage("§e----------------------------------\n\n\n");

    }
}
