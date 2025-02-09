package fr.mattmunich.civilisation_creatif.listeners;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.*;
import fr.mattmunich.civilisation_creatif.helpers.Utility;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.List;

public class EventListener implements Listener {

    private final ArrayList<Player> enterTerritoryName = new ArrayList<>();

    private final ArrayList<Player> defTerritoryBanner = new ArrayList<>();

    private final Main main;

    private final TerritoryData territoryData;

    public EventListener(Main main, TerritoryData territoryData) {
        this.main = main;
        this.territoryData = territoryData;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.setQuitMessage("§7[§c-§7] §e" + p.getDisplayName());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if(enterTerritoryName.contains(p)) {
            enterTerritoryName.remove(p);
            e.setCancelled(true);
            if(e.getMessage().contains("&")) {
                Bukkit.getScheduler().runTask(main, () -> p.chat("/territory"));
                return;
            }
            if(e.getMessage().length() > 15) {
                p.sendMessage(main.prefix + "§4Le nom du territroire doit faire au maximum §c15 caractères §4!");
                return;
            }
            if(!e.getMessage().matches("[a-zA-Z0-9]+")) {
                p.sendMessage(main.prefix + "§4Le nom du territroire ne doit pas contenir de §ccaractères spéciaux §8§o(seulement a-Z et 0-9) §4!");
                return;
            }

            Inventory chooseColorInv = Bukkit.createInventory(p, 27,"§aCouleur du territoire " + e.getMessage());
            chooseColorInv.setItem(0, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(1, ItemBuilder.getItem(Material.BLUE_CONCRETE, "§1Bleu foncé", false, false, null,null,null));
            chooseColorInv.setItem(2, ItemBuilder.getItem(Material.BLUE_WOOL, "§9Bleu clair", false, false, null,null,null));
            chooseColorInv.setItem(3, ItemBuilder.getItem(Material.LIGHT_BLUE_CONCRETE, "§bAqua", false, false, null,null,null));
            chooseColorInv.setItem(4, ItemBuilder.getItem(Material.CYAN_CONCRETE, "§3Cyan", false, false, null,null,null));
            chooseColorInv.setItem(5, ItemBuilder.getItem(Material.LIME_CONCRETE, "§aVert clair", false, false, null,null,null));
            chooseColorInv.setItem(6, ItemBuilder.getItem(Material.GREEN_CONCRETE, "§2Vert", false, false, null,null,null));
            chooseColorInv.setItem(7, ItemBuilder.getItem(Material.ORANGE_CONCRETE, "§6Orange/Or", false, false, null,null,null));
            chooseColorInv.setItem(8, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(9, ItemBuilder.getItem(Material.BLACK_CONCRETE, "§0Noir", false, false, null,null,null));
            chooseColorInv.setItem(10, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(11, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(12, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(13, ItemBuilder.getItem(Material.PAPER, "§a§oℹ Choisissez la couleur pour votre territoire", false, false, null,null,null));
            chooseColorInv.setItem(14, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(15, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(16, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(17, ItemBuilder.getItem(Material.YELLOW_CONCRETE, "§eJaune", false, false, null,null,null));
            chooseColorInv.setItem(18, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            chooseColorInv.setItem(19, ItemBuilder.getItem(Material.RED_WOOL, "§cRouge clair", false, false, null,null,null));
            chooseColorInv.setItem(20, ItemBuilder.getItem(Material.RED_CONCRETE, "§4Rouge foncé", false, false, null,null,null));
            chooseColorInv.setItem(21, ItemBuilder.getItem(Material.PURPLE_CONCRETE, "§5Voilet", false, false, null,null,null));
            chooseColorInv.setItem(22, ItemBuilder.getItem(Material.PINK_CONCRETE, "§dRose", false, false, null,null,null));
            chooseColorInv.setItem(23, ItemBuilder.getItem(Material.WHITE_CONCRETE, "§rBlanc", false, false, null,null,null));
            chooseColorInv.setItem(24, ItemBuilder.getItem(Material.LIGHT_GRAY_CONCRETE, "§7Gris clair", false, false, null,null,null));
            chooseColorInv.setItem(25, ItemBuilder.getItem(Material.GRAY_CONCRETE, "§8Gris foncé", false, false, null,null,null));
            chooseColorInv.setItem(26, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
            Bukkit.getScheduler().runTask(main, () -> p.openInventory(chooseColorInv));
            return;
        }
        if(defTerritoryBanner.contains(p) && e.getMessage().toLowerCase().contains("go")){
            e.setMessage("");
            e.setCancelled(true);
            if(!(p.getInventory().getItemInMainHand().getItemMeta() instanceof BannerMeta)) {
                p.sendMessage(main.prefix + "§4Cet objet n'est pas une bannière !");
                Bukkit.getScheduler().runTask(main, () -> p.openInventory(territoryData.getTerrInv(p, territoryData.getTerritoryTeamOfPlayer(p))));
                defTerritoryBanner.remove(p);
                return;
            }
            ItemStack banner = p.getInventory().getItemInMainHand();
            territoryData.setTerritoryBanner(territoryData.getPlayerTerritory(p), banner);
            p.sendTitle("§a✅ Succès","§2§oLa bannière de votre territoire a été définie !", 20, 100, 20);
            p.sendMessage(main.prefix + "§2La bannière de votre territoire a été définie !");
            defTerritoryBanner.remove(p);
            return;
        }

        PlayerData data = null;
        try {
            data = new PlayerData(p.getUniqueId());
        } catch (Exception err) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Une erreur s'est produite lors de l'envoi de se message ! §cEssayez de vous déconnecter et de vous reconnecter.");
            e.setFormat(main.hex(p.getDisplayName() + " §l§8>>§r "
                    + ChatColor.translateAlternateColorCodes('&', String.join(" ", e.getMessage()))));
            return;
        }

        Grades pRank = data.getRank();
        if(pRank == null) {
            p.sendMessage(main.prefix + "§4Une erreur s'est produite lors de l'envoi de se message ! §cEssayez de vous déconnecter et de vous reconnecter.");
            e.setCancelled(true);
            return;
        }
        String chatSeparator = pRank.getChatSeparator();

        e.setFormat(main.hex(p.getDisplayName() + chatSeparator
                + ChatColor.translateAlternateColorCodes('&', String.join(" ", e.getMessage()))));
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();
        p.sendTitle("§4§lVous avez été kick","§e§oTransfert vers §4M.§cJ.§6E.§eP.",20,100,20);
        p.sendMessage("§e(§6!§e) §4Vous avez été kick du serveur Civlisation Créatif avec la raison : §c" + e.getReason());
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS,120,255,false,false,false);
        p.addPotionEffect(blindness);
        Bukkit.getScheduler().runTaskLater(main, () -> {
            p.transfer("91.197.6.60", 25599);
        },120);
    }

    @EventHandler
    public void onCristal(EntityExplodeEvent e) {
        for (Entity nEntity : e.getEntity().getNearbyEntities(10, 10, 10)) {
            if (nEntity instanceof Player p) {
                p.sendMessage(main.prefix + "§4Les explosions sont désactivées sur le serveur !");
                Bukkit.getConsoleSender()
                        .sendMessage(main.prefix + "§c" + p.getName()
                                + "§4 a essayé de faire exploser une entité aux coordonnées : §cX: "
                                + e.getEntity().getLocation().getBlockX() + "§4, §cY: "
                                + e.getEntity().getLocation().getBlockY() + "§4, §cZ: "
                                + e.getEntity().getLocation().getBlockZ());
            }
        }
        e.getEntity().remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onExplosion(ExplosionPrimeEvent e) {
        for (Entity nEntity : e.getEntity().getNearbyEntities(10, 10, 10)) {
            if (nEntity instanceof Player p) {
                p.sendMessage(main.prefix + "§4Les TNTs sont désactivées sur le serveur !");
                Bukkit.getConsoleSender()
                        .sendMessage(main.prefix + "§c" + p.getName()
                                + "§4 a essayé de faire exploser une TNT aux coordonnées : §cX: "
                                + e.getEntity().getLocation().getBlockX() + "§4, §cY: "
                                + e.getEntity().getLocation().getBlockY() + "§4, §cZ: "
                                + e.getEntity().getLocation().getBlockZ());
            }
        }
        e.setFire(false);
        e.getEntity().remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onRespawnAnchorExplosion(BlockExplodeEvent e) {
        Block block = e.getBlock();
        ArrayList<String> suspects = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()){
            if(player.getLocation().distance(block.getLocation()) < 8) {
                suspects.add(player.getName());
            }
        }
        Bukkit.getConsoleSender()
                .sendMessage(main.prefix + "§cUn des joueurs dans la liste suivante "
                        + "§4 a essayé de faire exploser un block aux coordonnées : §cX: "
                        + e.getBlock().getLocation().getBlockX() + "§4, §cY: "
                        + e.getBlock().getLocation().getBlockY() + "§4, §cZ: "
                        + e.getBlock().getLocation().getBlockZ()
                        + " §4Suspects : §c" + suspects.stream());

        e.setCancelled(true);
        try {
            for(Block b : e.blockList()) {
                b.getWorld().setBlockData(b.getLocation(), b.getBlockData());
            }
        } catch (Exception err) {
            Bukkit.getConsoleSender().sendMessage("Couldn't replace blocks after explosion : " + err.fillInStackTrace());
        }
        e.setYield(0);
    }

    @EventHandler
    public void onInvInteract(InventoryClickEvent e) throws Exception {
        if(e.getWhoClicked() instanceof Player p) {
            InventoryView invView = e.getView();
            Inventory inv = e.getInventory();
            ItemStack it = e.getCurrentItem();
            if (invView.getTitle().equalsIgnoreCase("§6Menu §7- §a/territoire §7(§8§oAucun§7)")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                switch (Objects.requireNonNull(it).getType()) {
                    case CRAFTING_TABLE:
                        //CREATE TERRITORY
                        invView.close();
                        p.sendMessage(main.prefix + "§2Entrez le nom du territoire dans le tchat. §7(§o§c& §r§cpour annuler§7)");
                        enterTerritoryName.add(p);
                        break;
                    case END_CRYSTAL:
                        //JOIN TERRITORY
                        PlayerData data = new PlayerData(p.getUniqueId());
                        try {
                            data.getInvitesToTerritory();
                            if(data.getInvitesToTerritory().isEmpty()) {
                                //NO INVITES
                                Inventory invitesInv = Bukkit.createInventory(p, 54, "§bInvitations à rejoindre un territoire");
                                invitesInv.setItem(22, ItemBuilder.getItem(Material.PAPER, "§b§oVous n'avez aucune invitation à rejoindre un territoire...", true, false, null, null, null));
                                invitesInv.setItem(53, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
                                p.openInventory(invitesInv);
                            } else {
                                //HAS INVITES
                                Inventory invitesInv = Bukkit.createInventory(p, 54, "§bInvitations à rejoindre un territoire");
                                invitesInv.setItem(53, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));

                                List<Map<?, ?>> invites = data.getInvitesToTerritory();
                                for (Map<?, ?> invite : invites) {
                                    for (Map.Entry<?, ?> entry : invite.entrySet()) {
                                        Team territory = territoryData.getTerritoryTeam((String) entry.getKey());
                                        OfflinePlayer sender = Bukkit.getOfflinePlayer(UUID.fromString((String) entry.getValue()));

                                        ItemStack pHead = new ItemStack(Material.PLAYER_HEAD);
                                        SkullMeta phm = (SkullMeta) pHead.getItemMeta();
                                        assert phm != null;
                                        phm.setOwningPlayer(sender);
                                        phm.setDisplayName(territory.getColor() + territory.getName());
                                        phm.setLore(Arrays.asList("§a§oCliquez pour rejoindre le territoire " + territory.getColor() + territory.getName(), " ", "§2Invité par " + sender.getName()));
                                        pHead.setItemMeta(phm);
                                        invitesInv.addItem(pHead);
                                    }
                                }
                                p.openInventory(invitesInv);
                            }
                        } catch (Exception e1) {
                            p.sendMessage(main.prefix + "§4Impossible de charger vos invitations, §cveuillez signaler cela à un membre du staff !");

                        }

                        break;
                    case SPYGLASS:


//                        int pageNum = territoryData.getTerritoriesList().size()/28;
//                        Inventory terrListInv_Layout = getTerrListInv_Layout(p,1,pageNum);
//                        for(String terr : territoryData.getTerritoriesList()) {
//                            Team territory = territoryData.getTerritoryTeam(terr);
//                            Player chief = Bukkit.getPlayer(territoryData.getTerritoryChiefUUID(terr));
//                            String chiefName = "";
//                            if(chief==null){
//                                chiefName = "§c§oNon trouvé";
//                            } else {
//                                chiefName = chief.getName();
//                            }
//                            ItemStack banner = territoryData.getTerritoryBanner(terr);
//                            ItemMeta bannerMeta = banner.getItemMeta();
//                            int xp = territoryData.getTerritoryXP(terr);
//                            int money = territoryData.getTerritoryMoney(terr);
//                            assert bannerMeta != null;
//                            bannerMeta.setItemName(territory.getColor() + territory.getName());
//                            bannerMeta.setLore(Arrays.asList("§2Chef: §a" + chiefName,"§2XP:§a" + xp,"§2Argent:§a" + money));
//                        }
                        Inventory terrListInv = territoryData.getTerritoryListInventory(p, 1);
                        p.openInventory(terrListInv);
                        break;
                    case BARRIER:
                        e.getView().close();
                        break;
                    default:
                        break;
                }
            } else if (invView.getTitle().contains("§6Menu §7- §a/territoire §7(")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                switch (Objects.requireNonNull(it).getType()) {
                    case RED_DYE:
                        invView.close();
                        //LEAVE TERRITORY
                        Inventory confirmInv = Bukkit.createInventory(p, 9, "§cQuitter votre territoire ?");
                        confirmInv.setItem(3, ItemBuilder.getItem(Material.LIME_CONCRETE, "§aConfirmer", false, false, null, null, null));
                        confirmInv.setItem(4, ItemBuilder.getItem(Material.PAPER, "§e§oℹ Êtes-vous sûr de vouloir quitter votre territoire ?", true, false, null, null, null));
                        confirmInv.setItem(5, ItemBuilder.getItem(Material.RED_CONCRETE, "§cAnnuler", false, false, null, null, null));
                        p.openInventory(confirmInv);
                        break;
                    case PAPER:
                        //TERRITORY MENU
                        Team territory = territoryData.getTerritoryTeamOfPlayer(p);
                        Inventory terrInv = territoryData.getTerrInv(p, territory);
                        p.openInventory(terrInv);
                        break;
                    case SPYGLASS:
                        Inventory terrListInv = territoryData.getTerritoryListInventory(p,1);
                        p.openInventory(terrListInv);
                        break;
                    case BARRIER:
                        e.getView().close();
                        break;
                    default:
                        break;
                }
            } else if (invView.getTitle().equalsIgnoreCase("§cQuitter votre territoire ?")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                switch (Objects.requireNonNull(it).getType()) {
                    case LIME_CONCRETE:
                        //LEAVE
                        invView.close();
                        territoryData.leaveTerritory(p);
                        break;
                    case RED_CONCRETE:
                        //CANCEL
                        invView.close();
                        p.chat("/territoire");
                        break;
                    default:
                        break;
                }
            } else if(invView.getTitle().contains("§aCouleur du territoire ")){
                e.setCancelled(true);
                if(it ==null) {return;}
                String terrName = invView.getTitle().replace("§aCouleur du territoire ","");
                switch (Objects.requireNonNull(it).getType()) {
                    case BLUE_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.DARK_BLUE);
                        e.getView().close();
                        break;
                    case BLUE_WOOL:
                        territoryData.createTerritory(p, terrName, ChatColor.BLUE);
                        e.getView().close();
                        break;
                    case LIGHT_BLUE_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.AQUA);
                        e.getView().close();
                        break;
                    case CYAN_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.DARK_AQUA);
                        e.getView().close();
                        break;
                    case LIME_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.GREEN);
                        e.getView().close();
                        break;
                    case GREEN_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.DARK_GREEN);
                        e.getView().close();
                        break;
                    case ORANGE_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.GOLD);
                        e.getView().close();
                        break;
                    case YELLOW_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.YELLOW);
                        e.getView().close();
                        break;
                    case RED_WOOL:
                        territoryData.createTerritory(p, terrName, ChatColor.RED);
                        e.getView().close();
                        break;
                    case RED_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.DARK_RED);
                        e.getView().close();
                        break;
                    case PURPLE_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.DARK_PURPLE);
                        e.getView().close();
                        break;
                    case PINK_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.LIGHT_PURPLE);
                        e.getView().close();
                        break;
                    case WHITE_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.WHITE);
                        e.getView().close();
                        break;
                    case LIGHT_GRAY_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.GRAY);
                        e.getView().close();
                        break;
                    case GRAY_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.DARK_GRAY);
                        e.getView().close();
                        break;
                    case BLACK_CONCRETE:
                        territoryData.createTerritory(p, terrName, ChatColor.BLACK);
                        e.getView().close();
                        break;
                    default:
                        break;
                }
            } else if (invView.getTitle().contains("§aTerritoire : ")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                switch (Objects.requireNonNull(it).getType()) {
                    case END_CRYSTAL:
                        //INVITE PLAYER
                        invView.close();
                        Inventory inviteInv = Bukkit.createInventory(p, 54, "§bInviter un joueur au territoire");

                        inviteInv.setItem(53, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
                        for (OfflinePlayer all : Bukkit.getOfflinePlayers()) {
                            PlayerData playerData = new PlayerData(all.getUniqueId());
                            PlayerData senderData = new PlayerData(p.getUniqueId());

                            if(playerData.getTerritory() != null || all.getUniqueId() == p.getUniqueId() || Objects.equals(playerData.getTerritory(), senderData.getTerritory())){
                                continue;
                            }

                            inviteInv.addItem(playerData.getSkull(all,"§bCliquez pour inviter le joueur §5" + all.getName()));
                        }
                        p.openInventory(inviteInv);
                        break;
                    case CYAN_STAINED_GLASS:
                        //CHANGE COLOR
                        Inventory chooseColorInv = Bukkit.createInventory(p, 27,"§2Couleur du territoire " + territoryData.getTerritoryTeamOfPlayer(p).getColor() + territoryData.getTerritoryTeamOfPlayer(p).getName());
                        chooseColorInv.setItem(0, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(1, ItemBuilder.getItem(Material.BLUE_CONCRETE, "§1Bleu foncé", false, false, null,null,null));
                        chooseColorInv.setItem(2, ItemBuilder.getItem(Material.BLUE_WOOL, "§9Bleu clair", false, false, null,null,null));
                        chooseColorInv.setItem(3, ItemBuilder.getItem(Material.LIGHT_BLUE_CONCRETE, "§bAqua", false, false, null,null,null));
                        chooseColorInv.setItem(4, ItemBuilder.getItem(Material.CYAN_CONCRETE, "§3Cyan", false, false, null,null,null));
                        chooseColorInv.setItem(5, ItemBuilder.getItem(Material.LIME_CONCRETE, "§aVert clair", false, false, null,null,null));
                        chooseColorInv.setItem(6, ItemBuilder.getItem(Material.GREEN_CONCRETE, "§2Vert", false, false, null,null,null));
                        chooseColorInv.setItem(7, ItemBuilder.getItem(Material.ORANGE_CONCRETE, "§6Orange/Or", false, false, null,null,null));
                        chooseColorInv.setItem(8, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(9, ItemBuilder.getItem(Material.BLACK_CONCRETE, "§0Noir", false, false, null,null,null));
                        chooseColorInv.setItem(10, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(11, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(12, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(13, ItemBuilder.getItem(Material.PAPER, "§a§oℹ Choisissez la couleur pour votre territoire", false, false, null,null,null));
                        chooseColorInv.setItem(14, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(15, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(16, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(17, ItemBuilder.getItem(Material.YELLOW_CONCRETE, "§eJaune", false, false, null,null,null));
                        chooseColorInv.setItem(18, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        chooseColorInv.setItem(19, ItemBuilder.getItem(Material.RED_WOOL, "§cRouge clair", false, false, null,null,null));
                        chooseColorInv.setItem(20, ItemBuilder.getItem(Material.RED_CONCRETE, "§4Rouge foncé", false, false, null,null,null));
                        chooseColorInv.setItem(21, ItemBuilder.getItem(Material.PURPLE_CONCRETE, "§5Voilet", false, false, null,null,null));
                        chooseColorInv.setItem(22, ItemBuilder.getItem(Material.PINK_CONCRETE, "§dRose", false, false, null,null,null));
                        chooseColorInv.setItem(23, ItemBuilder.getItem(Material.WHITE_CONCRETE, "§rBlanc", false, false, null,null,null));
                        chooseColorInv.setItem(24, ItemBuilder.getItem(Material.LIGHT_GRAY_CONCRETE, "§7Gris clair", false, false, null,null,null));
                        chooseColorInv.setItem(25, ItemBuilder.getItem(Material.GRAY_CONCRETE, "§8Gris foncé", false, false, null,null,null));
                        chooseColorInv.setItem(26, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null,null,null));
                        p.openInventory(chooseColorInv);
                        break;
                    case RED_DYE:
                        invView.close();
                        //DELETE TERRITORY
                       try {
                           String territoryName = territoryData.getPlayerTerritory(p);
                           if(territoryData.isChief(p,territoryName)) {
                               Inventory confirmInv = Bukkit.createInventory(p, 9, "§4Supprimer votre territoire ?");
                               confirmInv.setItem(3, ItemBuilder.getItem(Material.LIME_CONCRETE, "§aConfirmer", false, false, null, null, null));
                               confirmInv.setItem(4, ItemBuilder.getItem(Material.PAPER, "§c§oℹ Êtes-vous sûr de vouloir supprimer votre territoire ?", true, false, null, null, null));
                               confirmInv.setItem(5, ItemBuilder.getItem(Material.RED_CONCRETE, "§cAnnuler", false, false, null, null, null));
                               p.openInventory(confirmInv);
                           } else {
                               p.sendMessage(main.prefix + "§4Vous n'avez pas la permission de supprimer ce territoire !");
                           }
                       } catch(NullPointerException exception) {
                           Inventory confirmInv = Bukkit.createInventory(p, 9, "§4Supprimer votre territoire ?");
                           confirmInv.setItem(3, ItemBuilder.getItem(Material.LIME_CONCRETE, "§aConfirmer", false, false, null, null, null));
                           confirmInv.setItem(4, ItemBuilder.getItem(Material.PAPER, "§c§oℹ Êtes-vous sûr de vouloir supprimer votre territoire ?", true, false, null, null, null));
                           confirmInv.setItem(5, ItemBuilder.getItem(Material.RED_CONCRETE, "§cAnnuler", false, false, null, null, null));
                           p.openInventory(confirmInv);
                           Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't get territory chief to verify a player's permission. §cAllowed " + p.getName() + " to delete territory " + territoryData.getTerritoryTeamOfPlayer(p).getName());
                       }
                        return;
                    case WHITE_BANNER,BLACK_BANNER,RED_BANNER,BLUE_BANNER,LIGHT_BLUE_BANNER,BROWN_BANNER,CYAN_BANNER,GRAY_BANNER,GREEN_BANNER,LIGHT_GRAY_BANNER,LIME_BANNER,MAGENTA_BANNER,ORANGE_BANNER,PINK_BANNER,PURPLE_BANNER,YELLOW_BANNER:
                        Bukkit.getScheduler().runTask(main, p::closeInventory);
                        p.sendTitle("§2Prenez la bannière","§2§ldans votre main",20,100,20);
                        p.sendMessage(main.prefix + "§2Prenez la §5future§2 bannière de territoire §2§ldans votre main§r§2.");
                        Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> {
                            p.sendTitle("§rQuand c'est fait","§2§lentrez §r§2\"GO\"§2§l dans le tchat",20,60,20);
                            p.sendMessage(main.prefix + "§2Lorsque c'est fait, entrez \"GO\" dans le tchat.");
                            defTerritoryBanner.add(p);
                        }, 100);
                        break;
                    case BARRIER:
                        invView.close();
                        break;
                }
            } else if(invView.getTitle().contains("§2Couleur du territoire ")){
                e.setCancelled(true);
                if(it ==null) {return;}
                Team territory = territoryData.getTerritoryTeamOfPlayer(p);
                switch (Objects.requireNonNull(it).getType()) {
                    case BLUE_CONCRETE:
                        territory.setColor(ChatColor.DARK_BLUE);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case BLUE_WOOL:
                        territory.setColor(ChatColor.BLUE);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case LIGHT_BLUE_CONCRETE:
                        territory.setColor(ChatColor.AQUA);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case CYAN_CONCRETE:
                        territory.setColor(ChatColor.DARK_AQUA);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case LIME_CONCRETE:
                        territory.setColor(ChatColor.GREEN);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case GREEN_CONCRETE:
                        territory.setColor(ChatColor.DARK_GREEN);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case ORANGE_CONCRETE:
                        territory.setColor(ChatColor.GOLD);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case YELLOW_CONCRETE:
                        territory.setColor(ChatColor.YELLOW);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case RED_WOOL:
                        territory.setColor(ChatColor.RED);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case RED_CONCRETE:
                        territory.setColor(ChatColor.DARK_RED);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case PURPLE_CONCRETE:
                        territory.setColor(ChatColor.DARK_PURPLE);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case PINK_CONCRETE:
                        territory.setColor(ChatColor.LIGHT_PURPLE);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case WHITE_CONCRETE:
                        territory.setColor(ChatColor.WHITE);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case LIGHT_GRAY_CONCRETE:
                        territory.setColor(ChatColor.GRAY);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case GRAY_CONCRETE:
                        territory.setColor(ChatColor.DARK_GRAY);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    case BLACK_CONCRETE:
                        territory.setColor(ChatColor.BLACK);
                        p.chat("/territoire");
                        e.getView().close();
                        break;
                    default:
                        break;
                }
            } else if (invView.getTitle().equalsIgnoreCase("§4Supprimer votre territoire ?")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                switch (Objects.requireNonNull(it).getType()) {
                    case LIME_CONCRETE:
                        //LEAVE
                        invView.close();
                        territoryData.deleteTerritory(p,territoryData.getTerritoryTeamOfPlayer(p).getName());
                        break;
                    case RED_CONCRETE:
                        //CANCEL
                        invView.close();
                        p.chat("/territoire");
                        break;
                    default:
                        break;
                }
            } else if (invView.getTitle().equalsIgnoreCase("§bInviter un joueur au territoire")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                if (it.getType().equals(Material.BARRIER)) {
                    invView.close();
                } else {
                    //INVITE PLAYER
                    String name = Objects.requireNonNull(it.getItemMeta()).getDisplayName();
                    territoryData.invitePlayer(p, Bukkit.getOfflinePlayer(Utility.getUUIDFromName(name)));
                    invView.close();
                }
            } else if (invView.getTitle().equalsIgnoreCase("§bInvitations à rejoindre un territoire")) {
                e.setCancelled(true);
                if(it ==null) {return;}
                if (Objects.requireNonNull(it).getType() == Material.BARRIER) {
                    invView.close();
                } else {
                    //INVITE PLAYER
                    String name = Objects.requireNonNull(it.getItemMeta()).getDisplayName().substring(2);
                    territoryData.joinTerritory(p, name);
                    territoryData.removeInvite(name, p);
                    invView.close();
                }
            } else if (invView.getTitle().contains("§aListe des territoires")){
                e.setCancelled(true);
                if(it==null) {return;}
                switch (it.getType()) {
                    case BARRIER -> {
                        invView.close();
                        break;
                    }
                    case RED_STAINED_GLASS -> {
                        invView.close();
                        int page = territoryData.extractTerrListPageNumber(invView.getTitle());
                        Inventory terrListInv = territoryData.getTerritoryListInventory(p, page - 1);
                        p.openInventory(terrListInv);
                        break;
                    }
                    case LIME_STAINED_GLASS -> {
                        invView.close();
                        int page = territoryData.extractTerrListPageNumber(invView.getTitle());
                        Inventory terrListInv = territoryData.getTerritoryListInventory(p, page + 1);
                        p.openInventory(terrListInv);
                        break;
                    }
                    case WHITE_BANNER, BLACK_BANNER, RED_BANNER, BLUE_BANNER, LIGHT_BLUE_BANNER, BROWN_BANNER,
                         CYAN_BANNER, GRAY_BANNER, GREEN_BANNER, LIGHT_GRAY_BANNER, LIME_BANNER, MAGENTA_BANNER,
                         ORANGE_BANNER, PINK_BANNER, PURPLE_BANNER, YELLOW_BANNER -> {
                        Team territory = territoryData.getTerritoryTeamFromItem(it);
                        if (territory != null) {
                            Bukkit.getScheduler().runTask(main, p::closeInventory);
                            Inventory terrInv = territoryData.getTerrInv(p,territory);
                            Bukkit.getScheduler().runTask(main, () -> p.openInventory(terrInv));
                            break;
                        } else {
                            p.sendMessage(main.prefix + "§4Une erreur s'est produite - territoire non trouvé !");
                        }
                    }
                    default -> {
                        break;
                    }
                }
            }
        }
    }



    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        Player p = e.getPlayer();


        //noinspection ConstantValue
        if (entity == null) {
            return;
        }

        if (entity.getType().equals(EntityType.ARMOR_STAND)) {
            try {
                ArmorStand as = (ArmorStand) entity;
                String name = as.getName();
                if(name.contains("D") && name.contains("y") && name.contains("n") && name.contains("M") && name.contains("a") && name.contains("p")) {
                    //[{"text":"[","color":"yellow"},{"text":"Civilisation","color":"dark_green"},{"text":"] ","color":"yellow"},{"text":"{","color":"dark_purple","clickEvent":{"action":"open_url","value":"https://livemap.minestrator.com/s/40eb63e5-13e2-41c9-bb87-ee1b79ab0d54/"}},{"text":"Cliquez ici pour voir la DynMap","color":"light_purple","clickEvent":{"action":"open_url","value":"https://livemap.minestrator.com/s/40eb63e5-13e2-41c9-bb87-ee1b79ab0d54/"}},{"text":"}","color":"dark_purple","clickEvent":{"action":"open_url","value":"https://livemap.minestrator.com/s/40eb63e5-13e2-41c9-bb87-ee1b79ab0d54/"}}]
                    BaseComponent prefix =
                            new ComponentBuilder("[").color(net.md_5.bungee.api.ChatColor.YELLOW)
                                    .append("Civilisation").color(net.md_5.bungee.api.ChatColor.DARK_GREEN)
                                    .append("] ").color(net.md_5.bungee.api.ChatColor.YELLOW).build();
                    BaseComponent link =
                            new ComponentBuilder("{").color(net.md_5.bungee.api.ChatColor.DARK_PURPLE)
                                    .append("Cliquez pour voir la DynMap").color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE)
                                    .append("}").color(net.md_5.bungee.api.ChatColor.DARK_PURPLE).build();
                    link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://livemap.minestrator.com/s/40eb63e5-13e2-41c9-bb87-ee1b79ab0d54/"));
                    p.spigot().sendMessage(prefix,link);
                }
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ex.getMessage() + Arrays.toString(ex.getStackTrace()).replace(",", ",\n"));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(main.seeTerritoryBorders.contains(p)) {
//            p.sendMessage("Hey! Let's show you the nearby claimed chunks...");
            Location loc = p.getLocation();
            Chunk chunk = loc.getChunk();
            World world = loc.getWorld();
            int range = 10;
//            p.sendMessage("Scanning for chunks...");
            for (int chunkX = chunk.getX() - range; chunkX < chunk.getX() + range; chunkX++) {
                for (int chunkZ = chunk.getZ() - range; chunkZ < chunk.getZ() + range; chunkZ++) {
//                    p.sendMessage("Checking chhunk" + chunkX + " ; " + chunkZ);
                    assert world != null;
                    Chunk chunkToShow = world.getChunkAt(chunkX,chunkZ);
                    Map<Integer,Integer> chunkTS = territoryData.getChunkMap(chunkToShow);
                    if (territoryData.getChunkOwner(chunkTS) != null && !territoryData.getChunkOwner(chunkTS).isEmpty()) {
//                        p.sendMessage("Rendering chunk...");
                        ChatColor chatColor = territoryData.getTerritoryTeam(territoryData.getChunkOwner(chunkTS)).getColor();
                        territoryData.showChunkBorder(chunkToShow, chatColor, p);
                    }

                }
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        try {
            PlayerData playerData = new PlayerData(p.getUniqueId());
            playerData.addMoney(1);
            if(territoryData.getChunkOwner(territoryData.getChunkMap(p.getLocation().getChunk())) !=null && territoryData.getChunkOwner(territoryData.getChunkMap(p.getLocation().getChunk())).equals(territoryData.getPlayerTerritory(p))) {
                territoryData.addTerritoryXP(territoryData.getPlayerTerritory(p), 1);
                BaseComponent baseComponent = new ComponentBuilder()
                        .append("[").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("Argent").color(net.md_5.bungee.api.ChatColor.DARK_GREEN)
                        .append("] ").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("+").color(net.md_5.bungee.api.ChatColor.WHITE)
                        .append("1").color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append(main.moneySign).color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append(" & ").color(net.md_5.bungee.api.ChatColor.YELLOW)
                        .append("[").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("XP ").color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append("Territoire").color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE)
                        .append("] ").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("+").color(net.md_5.bungee.api.ChatColor.WHITE)
                        .append("1 XP").color(net.md_5.bungee.api.ChatColor.GREEN)
                        .build();
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponent);
            } else {
                BaseComponent baseComponent = new ComponentBuilder()
                        .append("[").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("Argent").color(net.md_5.bungee.api.ChatColor.DARK_GREEN)
                        .append("] ").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("+").color(net.md_5.bungee.api.ChatColor.WHITE)
                        .append("1").color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append(main.moneySign).color(net.md_5.bungee.api.ChatColor.GREEN)
                        .build();
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponent);
            }
        } catch (Exception ex) {
            main.logError("Couldn't give reward to player for placing block",ex);
        }

    }

    @EventHandler
    public void onSpawnVillager(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getMaterial().equals(Material.VILLAGER_SPAWN_EGG)) {
            e.setCancelled(true);
            try {
                SpawnEggMeta meta = (SpawnEggMeta) e.getItem().getItemMeta();
                territoryData.spawnWorker(p,meta,e.getClickedBlock() != null ? e.getClickedBlock().getLocation() : p.getLocation(), e.getItem());
                return;
            } catch (ClassCastException ignored) {}
        }
    }

    @EventHandler
    public void onVillagerDeath(EntityDeathEvent e) {
        if(e.getEntity() instanceof Villager villager) {
            UUID workerUUID = null;
            for (String tag : villager.getScoreboardTags()) {
                if (tag.contains("workerUUID=")) {
                    workerUUID = UUID.fromString(tag.replace("workerUUID=",""));
                }
            }
            if(workerUUID == null || !territoryData.getWorkerList().contains(workerUUID.toString())) {
                return;
            }
            String workerType = null;
            for (String tag : villager.getScoreboardTags()) {
                if (tag.contains("workerType=")) {
                    workerType = tag.replace("workerType=","");
                }
            }
            String territoryName = territoryData.getWorkerTerritory(workerUUID);
            territoryData.getConfig().set("territories." + territoryName + ".villagers." + workerUUID + ".isSpawned", false);
            territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué !" : "§4Un employé de type §e" + workerType + " a été tué !");
        }
    }
}
