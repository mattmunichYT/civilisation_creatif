package fr.mattmunich.civilisation_creatif.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fr.mattmunich.civilisation_creatif.helpers.Grades;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.Warp;


public class WarpCommand implements CommandExecutor, TabCompleter {

	private Main main;

	private Warp warp;

	public WarpCommand(Main main, Warp warp) {
		this.main = main;
		this.warp = warp;
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {

		if(s instanceof BlockCommandSender) {
			s.sendMessage("§4Utilisation de Command Blocks désactivée !");
			return true;
		}

		if(!(s instanceof Player)) {
			s.sendMessage(main.prefix + "§4Vous devez etre un joueur pour utiliser cette commande !");
			return true;
		}

		Player p = (Player) s;

		//EVENT PAQUES
//		if(!main.staff.contains(p)) {
//			p.sendMessage(main.getErrorPrefix() + "Cette commande est désactivée durant l'Évent de Pâques !");
//			return true;
//		}
		//END

		if(l.equalsIgnoreCase("setwarp")) {

			if(!(main.buildeur.contains(p))) {
				p.sendMessage(main.noPermToExc);
				return true;
			}

			if(args.length > 2 || args.length < 1) {
				p.sendMessage("§cSintax : /setwarp <warpName> [permission]");
				return true;
			}

			//Définir les variables
			String warpName = args[0];
			String worldName = p.getLocation().getWorld().getName();
			int x = p.getLocation().getBlockX();
			int y = p.getLocation().getBlockY();
			int z = p.getLocation().getBlockZ();
			float pitch = p.getLocation().getPitch();
			float yaw = p .getLocation().getYaw();

			if(args.length == 2) {

				Grades grades = null;

				try {
					grades = Grades.getGradeById(Integer.parseInt(args[1]));
				} catch(NumberFormatException nbe){
					try {
						grades = Grades.valueOf(args[1].toUpperCase());
					}catch(Exception e) {
						p.sendMessage(main.prefix + "§4Grade non trouvé !");
						return true;
					}
					int id = grades.getId();
					warp.setWarp(warpName, p, worldName, x, y, z, pitch, yaw, id);
				}


			}else {
				//Utiliser la fonction setWarp()
				warp.setWarp(warpName, p, worldName, x, y, z, pitch, yaw, 1);
			}



			return true;
		}else if (l.equalsIgnoreCase("warp")) {
			if(args.length != 1) {
				warp.warpListSendMsg(p);
				p.sendMessage("§cSintax : /warp <warpName>");
				return true;
			}
			String warpName = args[0];

			//Utiliser la fonction warp()
			warp.warp(warpName, p);

			return true;
		}else if(l.equalsIgnoreCase("delwarp")) {

			if(!(main.buildeur.contains(p))) {
				p.sendMessage(main.noPermToExc);
				return true;
			}

			if(args.length != 1) {
				p.sendMessage("§cSintax : /delwarp <warpName>");
				return true;
			}

			String warpName = args[0];

			//Utiliser la fonction delWarp()
			warp.delWarp(warpName, p);

			return true;

		}


		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tabComplete = Lists.newArrayList();
		if(label.equalsIgnoreCase("setwarp")) {
			if(args.length == 2) {
				for(Grades grades : Grades.values()) {
					if(grades.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						tabComplete.add(grades.getName().toLowerCase());
					}
				}
			}
		}

		if(label.equalsIgnoreCase("warp")) {
            String warps = Objects.requireNonNull(warp.getConfig().get("warp.list")).toString();
            if (!warps.isEmpty()) {
                tabComplete = Arrays.asList(warps.split(","));
            }

			if (args.length == 1) {
				for (String a : tabComplete) {
					if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
						tabComplete.add(a);
					}
				}
			}
		}


		return tabComplete;
	}

}
