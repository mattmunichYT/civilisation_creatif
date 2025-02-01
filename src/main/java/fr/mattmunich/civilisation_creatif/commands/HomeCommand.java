package fr.mattmunich.civilisation_creatif.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.Utility;
import fr.mattmunich.civilisation_creatif.helpers.PlayerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HomeCommand implements CommandExecutor, TabCompleter {

	private final Main main;

	public HomeCommand(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


		if(sender instanceof BlockCommandSender) {
			sender.sendMessage("§4Utilisation de Command Blocks désactivée !");
			return true;
		}
		//EVENT
//		if(sender instanceof Player) {
//			Player p = (Player)sender;
//			if(!main.staff.contains(p)) {
//				p.sendMessage(main.getErrorPrefix() + "Cette commande est désactivée durant l'Évent de Pâques !");
//				return true;
//			}
//		}
		//END


		if(label.equalsIgnoreCase("sethome") || label.equalsIgnoreCase("seth"))  {

			if(!(sender instanceof Player player)) {
				sender.sendMessage(main.prefix + "§4Vous devez etre un joueur pour utiliser cette commande !");
				return true;
			}

            if(!(main.modo.contains(player))) {
				player.sendMessage(main.noPermToExc);
				return true;
			}

            PlayerData data;
            try {
                data = new PlayerData(Utility.getUUIDFromName(player.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if(args.length != 1) {
				player.sendMessage("§cSintax : /sethome <HomeName>");
				return true;
			}
			data.getConfig().createSection("homes");
			if(!data.getConfig().isSet("home.")) {
				MemorySection.createPath(Objects.requireNonNull(data.getConfig().getConfigurationSection("homes")), "home");
                MemorySection.createPath(Objects.requireNonNull(data.getConfig().getConfigurationSection("homes")), "home.list");
			}
			data.getConfig().set("home."  + args[0] + ".world", Objects.requireNonNull(player.getLocation().getWorld()).getName());
			data.getConfig().set("home."  + args[0] + ".x", player.getLocation().getX());
			data.getConfig().set("home."  + args[0] + ".y", player.getLocation().getY());
			data.getConfig().set("home."  + args[0] + ".z", player.getLocation().getZ());
			data.getConfig().set("home."  + args[0] + ".pitch",player.getEyeLocation().getPitch());
			data.getConfig().set("home."  + args[0] + ".yaw", player.getEyeLocation().getYaw());
			data.saveConfig();
			if(data.getConfig().isSet("home.count")) {
				data.getConfig().set("home.count", data.getConfig().getInt("home.count") + 1);
			}else{
				data.getConfig().set("home.count", 1);
			}
			data.saveConfig();

			if(!data.getConfig().isSet("home.list")) {
				MemorySection.createPath(Objects.requireNonNull(data.getConfig().getConfigurationSection("homes")), "home.list");

				data.getConfig().set("home.list", args[0] + ",");
				player.sendMessage(main.prefix + "§2Le home \"§6" + args[0] + "§2\" à été défini à votre position !");
				data.saveConfig();
			}else {
				if(!data.getConfig().contains(args[0])) {
					data.getConfig().set("home.list", data.getConfig().get("home.list") + args[0] + ",");
					player.sendMessage(main.prefix + "§2Le home \"§6" + args[0] + "§2\" à été défini à votre position !");
				}else {
					player.sendMessage(main.prefix + "§2Le home \"§6" + args[0] + "§2\" à été redéfini à votre position !");
				}

				data.saveConfig();
			}
			data.saveConfig();

            PlayerData rldata;
            try {
                rldata = new PlayerData(Utility.getUUIDFromName(player.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            rldata.getHomes();

			return true;
		}

		if(label.equalsIgnoreCase("home") || label.equalsIgnoreCase("h")) {

			if(!(sender instanceof Player player)) {
				sender.sendMessage(main.prefix + "§4Vous devez etre un joueur pour utiliser cette commande !");
				return true;
			}

            PlayerData data;
            try {
                data = new PlayerData(Utility.getUUIDFromName(player.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if(args.length > 1 && args.length <= 3) {
				if(main.admin.contains(player)) {
					String tName = args[0];
                    PlayerData tdata;
                    try {
                        tdata = new PlayerData(Utility.getUUIDFromName(tName));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if(args[1].equalsIgnoreCase("home") && args[2].equalsIgnoreCase("list")) {
						if(tdata.getHomes() == null) {
							player.sendMessage("§e--------§2§lHomes§e--------\n§4Ce joueur n'a pas de homes !");
						}else {
							player.sendMessage("§e--------§2§lHomes§e--------\n§2Homes de " + tName + "  : §r\n§l§6" + tdata.getHomes().replace(",", ", "));
						}

                    }
//					else if(args.length == 3 || args.length == 1){
//						player.sendMessage(main.prefix + "§cMatt ! Tu te trompes voici les possibilités : \n"
//								+ "§2/home <joueur> <NomDuHome> §cou\n"
//								+ "§2/home <joueur> home list §8(liste des homes)");
//						return true;
//					}
					else {
						if(!tdata.getHomes().contains(args[1])) {
							player.sendMessage(main.prefix + "§4Le joueur n'a défini le home \"§6" + args[1] + "§4\" !");
							return true;
						}
						if(tdata.getConfig().contains("home." + args[1] + ".")) {
							World w = Bukkit.getWorld(Objects.requireNonNull(tdata.getConfig().getString("home." + args[1] + ".world")));
							double x = tdata.getConfig().getDouble("home."  + args[1] + ".x");
							double y = tdata.getConfig().getDouble("home."  + args[1] + ".y");
							double z = tdata.getConfig().getDouble("home."  + args[1] + ".z");
							double pitch = tdata.getConfig().getDouble("home."  + args[1] + ".pitch");
							double yaw = tdata.getConfig().getDouble("home."  + args[1] + ".yaw");

							try {
								player.teleport(new Location(w, x, y, z, (float) yaw, (float) pitch));
							} catch (Exception e) {
								player.sendMessage(main.prefix + "§4Le monde n'est pas chargé. §ePour accéder au Home, veuillez charger le monde §a\"§6" + tdata.getConfig().getString("home."  + args[1] + ".world") + "§a\"§e !");
							}
							
							//success
							player.sendMessage(main.prefix + "§2Vous avez été téléporté au home \"§6" + args[1] + "§2\" de " + tName + " !");
                        } else {
							player.sendMessage(main.prefix + "§4Le joueur n'a défini le home \"§6" + args[1] + "§4\" !");
                        }
                    }
                    return true;
                }
			}

			if(args.length != 1) {

				if(data.getHomes() == null) {
					player.sendMessage("§e--------§2§lHomes§e--------\n§8§oVous n'avez pas de homes\n§8§oCréez des homes avec /sethome <NomDuHome>");
				}else {
					player.sendMessage("§e--------§2§lHomes§e--------\n§2Vos Homes : §r\n§l§6" + data.getHomes().replace(",", ", "));
				}
				player.sendMessage("§cSintax : /home <HomeName>");
				return true;
			}

			if(data.getConfig().contains("home." + args[0] + ".")) {
				World w = Bukkit.getServer().getWorld(Objects.requireNonNull(data.getConfig().getString("home." + args[0] + ".world")));
				double x = data.getConfig().getDouble("home."  + args[0] + ".x");
				double y = data.getConfig().getDouble("home."  + args[0] + ".y");
				double z = data.getConfig().getDouble("home."  + args[0] + ".z");
				double pitch = data.getConfig().getDouble("home."  + args[0] + ".pitch");
				double yaw = data.getConfig().getDouble("home."  + args[0] + ".yaw");

				player.teleport(new Location(w, x, y, z, (float) yaw, (float) pitch));
				player.sendMessage(main.prefix + "§2Vous avez été téléporté au home \"§6" + args[0] + "§2\" !");
            } else {
				player.sendMessage(main.prefix + "§4Le home \"§6" + args[0] + "§4\" n'existe pas !");
            }
            return true;
        }

		if(label.equalsIgnoreCase("delhome") || label.equalsIgnoreCase("delh")) {

			if(!(sender instanceof Player player)) {
				sender.sendMessage(main.prefix + "§4Vous devez etre un joueur pour utiliser cette commande !");
				return true;
			}

            PlayerData data;
            try {
                data = new PlayerData(Utility.getUUIDFromName(player.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if(args.length != 1) {
				player.sendMessage("§cSintax : /delhome <HomeName>");
				return true;
			}

			if(data.getConfig().contains("home."  + args[0] + ".")) {
				if(Objects.requireNonNull(data.getConfig().getString("home.list")).contains(args[0])) {
					String result = Objects.requireNonNull(data.getConfig().getString("home.list")).replace(args[0] + ","  , "");

					data.getConfig().set("home.list", result);
					data.saveConfig();
				}else {
					player.sendMessage(main.prefix + "Une erreur s'est produite lors de la suppression du home. Annulation...");
					return true;
				}
				data.getConfig().set("home."  + args[0], null);
				data.getConfig().set("home.count", data.getConfig().getInt("home.count") - 1);

				player.sendMessage(main.prefix + "§2Le home \"§6" + args[0] + "§2"+ "§2\" a été supprimé !");
				data.saveConfig();
            } else {
				player.sendMessage(main.prefix + "§4Le home \"§6" + args[0] + "§4\" n'existe pas !");
            }
            return true;

        }

		return true;
	}

	List<String> arguments = new ArrayList<>();
	@Override
	public List<String> onTabComplete(CommandSender s, Command c, String l, String[] args) {
		if(arguments.isEmpty()) {

			Player p = (Player) s;

            PlayerData data = null;
            try {
                data = new PlayerData(Utility.getUUIDFromName(p.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String homeList = data.getHomes();
			arguments = Arrays.asList(homeList.split(","));
		}

		List<String> result = new ArrayList<>();
		if(args.length == 1) {
			for (String a : arguments) {
				if(a.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(a);
				}
			}
			return result;
		}
		return null;
	}


}
