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
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.List;

public class EventListener implements Listener {

    private final ArrayList<Player> enterTerritoryName = new ArrayList<>();

    private final ArrayList<Player> defTerritoryBanner = new ArrayList<>();

    private final ArrayList<Player> enterNewTerritoryName = new ArrayList<>();

    private final ArrayList<Player> enterNewTerritoryDescription = new ArrayList<>();

    private final Main main;

    private final Plugin plugin;

    private final TerritoryData territoryData;

    public EventListener(Main main, Plugin plugin, TerritoryData territoryData) {
        this.main = main;
        this.plugin = plugin;
        this.territoryData = territoryData;
    }

    private final Map<UUID, Integer> moneyGained = new HashMap<>();
    private final Map<UUID, Integer> xpGained = new HashMap<>();
    private final Map<UUID, BukkitTask> resetTasks = new HashMap<>();
    private final Map<Player, String> currentChunkOwner = new HashMap<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.setQuitMessage(main.leaveMessage(p));
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
            if(e.getMessage().length() > 20) {
                p.sendMessage(main.prefix + "§4Le nom du territroire doit faire au maximum §c20 caractères §4!");
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
        if(enterNewTerritoryName.contains(p)) {
            enterNewTerritoryName.remove(p);
            e.setCancelled(true);
            if(territoryData.getPlayerTerritory(p)==null || !territoryData.isChief(p,territoryData.getPlayerTerritory(p))) {
                p.sendMessage(main.prefix + "§4Vous ne pouvez pas faire cela.");
                return;
            }
            if(e.getMessage().equals("&")){
                p.sendMessage(main.prefix + "§eOpération annulée.");
                return;
            }
            if(e.getMessage().length() > 20) {
                p.sendMessage(main.prefix + "§4Le nom du territroire doit faire au maximum §c20 caractères §4!");
                return;
            }
            if(!e.getMessage().matches("[a-zA-Z0-9éèê]+")) {
                p.sendMessage(main.prefix + "§4Le nom du territroire ne doit pas contenir de §ccaractères spéciaux §8§o(seulement a-Z et 0-9) §4!");
                return;
            }
            ChatColor territoryColor = territoryData.getTerritoryTeam(territoryData.getPlayerTerritory(p)).getColor();
            try {
                territoryData.renameTerritory(territoryData.getPlayerTerritory(p),e.getMessage());
            } catch (NullPointerException ex) {
                p.sendMessage(main.prefix + "§4Une erreur s'est produite");
            }
            p.sendMessage(main.prefix + "§2Votre territoire a été renommé à " + territoryColor + e.getMessage() + "§2 !");
            return;
        }

        if(enterNewTerritoryDescription.contains(p)) {
            enterNewTerritoryDescription.remove(p);
            e.setCancelled(true);
            if(territoryData.getPlayerTerritory(p)==null || !territoryData.isChief(p,territoryData.getPlayerTerritory(p))) {
                p.sendMessage(main.prefix + "§4Vous ne pouvez pas faire cela.");
                return;
            }
            if(e.getMessage().equals("&")){
                p.sendMessage(main.prefix + "§eOpération annulée.");
                return;
            }
            if(e.getMessage().length() > 100) {
                p.sendMessage(main.prefix + "§4Le nom du territroire doit faire au maximum §c100 caractères §4!");
                return;
            }
            territoryData.setTerritoryDescription(territoryData.getPlayerTerritory(p),main.hex(e.getMessage()));
            p.sendMessage(main.prefix + "§2La description de votre territoire a été définie à :");
            p.sendMessage("§a" + main.hex(e.getMessage()));
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
                        if(territoryData.isOfficer(p, territoryData.getPlayerTerritory(p))) {return;}
                        //INVITE PLAYER
                        invView.close();
//                        p.sendTitle("§2§o🚀 Chargement du menu...","",20,500,20);
                        Inventory preInviteInv = Bukkit.createInventory(p, 54, "§bInviter un joueur au territoire");

                        preInviteInv.setItem(53, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
                        for (OfflinePlayer all : Bukkit.getOfflinePlayers()) {
                            PlayerData playerData = new PlayerData(all.getUniqueId());
                            PlayerData senderData = new PlayerData(p.getUniqueId());

                            if(playerData.getTerritory() != null || all.getUniqueId() == p.getUniqueId() || Objects.equals(playerData.getTerritory(), senderData.getTerritory())){
                                continue;
                            }
                            ItemStack playerSkull = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta skullMeta = (SkullMeta) playerSkull.getItemMeta();
                            skullMeta.setOwnerProfile(all.getPlayerProfile());
                            skullMeta.setLore(Collections.singletonList("§bCliquez pour inviter le joueur §5"));
                            skullMeta.setDisplayName(all.getName());
                            playerSkull.setItemMeta(skullMeta);
                            preInviteInv.addItem(playerSkull);
                        }
                        p.openInventory(preInviteInv);
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
                        p.closeInventory();
                        p.openInventory(inviteInv);
                        break;
                    case CYAN_STAINED_GLASS:
                        if(territoryData.isOfficer(p, territoryData.getPlayerTerritory(p))) {return;}
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
                        if(territoryData.isChief(p, territoryData.getPlayerTerritory(p))) {return;}
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
                    case VILLAGER_SPAWN_EGG:
                        if(!territoryData.isOfficer(p, territoryData.getPlayerTerritory(p)) && !territoryData.isChief(p, territoryData.getPlayerTerritory(p))) {return;}
                        //MANAGER WORKERS
                        p.closeInventory();
                        p.openInventory(territoryData.getTerritoryWorkersInventory(p, territoryData.getPlayerTerritory(p), 1));
                        break;
                    case PLAYER_HEAD:
                        if(!territoryData.isOfficer(p, territoryData.getPlayerTerritory(p)) && !territoryData.isChief(p, territoryData.getPlayerTerritory(p))) {return;}
                        //MANAGE MEMBERS
                        territoryData.showTerritoryMembersInventory(p, territoryData.getPlayerTerritory(p), 1);
                        break;
                    case WRITABLE_BOOK:
                        p.closeInventory();
                        p.sendMessage(main.prefix + "§2Envoyez la §ofuture §r§5description§2 de votre territoire dans le tchat. §e(§6§o& §r§epour annuler)");
                        p.sendMessage(main.prefix + "§a§oℹ Vous pouvez entrer au maximum 100 caractères");
                        p.sendTitle("§2Envoyez la description","§2§ldans le tchat",20,100,20);
                        enterNewTerritoryDescription.add(p);
                        break;
                    case OAK_SIGN:
                        p.sendMessage(main.prefix + "§cFonctionnalité désactivée.");
                        break;
//                        p.closeInventory();
//                        p.sendMessage(main.prefix + "§2Envoyez le §ofutur §r§5nom§2 de votre territoire dans le tchat. §e(§6§o& §r§epour annuler)");
//                        p.sendMessage(main.prefix + "§a§oℹ Vous pouvez entrer au maximum 20 caractères");
//                        p.sendTitle("§2Envoyez le nom","§2§ldans le tchat",20,100,20);
//                        enterNewTerritoryName.add(p);
//                        break;
                    case BARRIER:
                        invView.close();
                        break;
                    default:
                        if(Tag.BANNERS.isTagged(it.getType())){
                            if(territoryData.isOfficer(p, territoryData.getPlayerTerritory(p))) {return;}
                            Bukkit.getScheduler().runTask(main, p::closeInventory);
                            p.sendTitle("§2Prenez la bannière","§2§ldans votre main",20,100,20);
                            p.sendMessage(main.prefix + "§2Prenez la §5future§2 bannière de territoire §2§ldans votre main§r§2.");
                            Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> {
                                p.sendTitle("§rQuand c'est fait","§2§lentrez §r§2\"GO\"§2§l dans le tchat",20,60,20);
                                p.sendMessage(main.prefix + "§2Lorsque c'est fait, entrez \"GO\" dans le tchat.");
                                defTerritoryBanner.add(p);
                            }, 100);
                            return;
                        }
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
                        int page = territoryData.extractInventoryPageNumber(invView.getTitle());
                        Inventory terrListInv = territoryData.getTerritoryListInventory(p, page - 1);
                        p.openInventory(terrListInv);
                        break;
                    }
                    case LIME_STAINED_GLASS -> {
                        invView.close();
                        int page = territoryData.extractInventoryPageNumber(invView.getTitle());
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
            } else if (invView.getTitle().contains("§6Acheter un villegeois")){
                e.setCancelled(true);
                if(it == null || it.getType()==Material.GRAY_STAINED_GLASS_PANE) { return; }
                if (it.getType() == Material.BARRIER) {
                    invView.close();
                    return;
                } else {
                    WorkerType workerType = null;
                    for (WorkerType checkWorkerType : WorkerType.values()){
                        if(it.getType().equals(checkWorkerType.getItem())){
                            workerType = checkWorkerType;
                            break;
                        }
                    }
                    if(workerType==null) { return; }
                    invView.close();
                    territoryData.openChooseTierInv(p, workerType);
                    return;
                }
            } else if (invView.getTitle().contains("§6Choisir le tier du villageois")) {
                e.setCancelled(true);
                if (it == null || it.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                    return;
                }
                if(invView.getItem(13)==null){
                    p.sendMessage(main.prefix + "§4Une erreur s'est produite !");
                    return;
                }
                WorkerType type = null;
                String parsedType=invView.getItem(13).getItemMeta().getDisplayName().replace("§aℹ Choisissez le tier de votre villageois ","").replace(" ","_").toUpperCase();
                try {
                    type = WorkerType.valueOf(parsedType);
                } catch (IllegalArgumentException ex) {
                    p.sendMessage(main.prefix + "§4Une erreur s'est produite !");
                    main.logError("Couldn't get WorkerType from Paper Item. Tried valueOf(" + parsedType + "). This was ",ex);
                    return;
                }
                switch (it.getType()){
                    case BARRIER -> {
                        invView.close();
                        break;
                    }
                    case COAL_BLOCK -> {
                        invView.close();
                        territoryData.buyWorker(p,type,0);
                        break;
                    }
                    case IRON_BLOCK -> {
                        invView.close();
                        territoryData.buyWorker(p,type,1);
                        break;
                    }
                    case GOLD_BLOCK -> {
                        invView.close();
                        territoryData.buyWorker(p,type,2);
                        break;
                    }
                    case EMERALD_BLOCK -> {
                        invView.close();
                        territoryData.buyWorker(p,type,3);
                        break;
                    }
                    case DIAMOND_BLOCK -> {
                        invView.close();
                        territoryData.buyWorker(p,type,4);
                        break;
                    }
                    case NETHERITE_BLOCK -> {
                        invView.close();
                        territoryData.buyWorker(p,type,5);
                        break;
                    }
                }
            } else if (invView.getTitle().contains("§bGérer vos villageois §7- §ePage §6")) {
                e.setCancelled(true);
                if(it==null) {return;}
                switch (it.getType()) {
                    case BARRIER -> invView.close();
                    case RED_STAINED_GLASS -> {
                        invView.close();
                        int page = territoryData.extractInventoryPageNumber(invView.getTitle());
                        Inventory workerListInv = territoryData.getTerritoryWorkersInventory(p, territoryData.getPlayerTerritory(p), page - 1);
                        p.openInventory(workerListInv);
                    }
                    case LIME_STAINED_GLASS -> {
                        invView.close();
                        int page = territoryData.extractInventoryPageNumber(invView.getTitle());
                        Inventory workerListInv = territoryData.getTerritoryWorkersInventory(p, territoryData.getPlayerTerritory(p), page + 1);
                        p.openInventory(workerListInv);
                    }
                    case VILLAGER_SPAWN_EGG -> {
                        p.closeInventory();
                        territoryData.showBuyWorkerInv(p);
                    }
                    default -> {
                        if(it.getItemMeta() != null && it.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"workerUUID"),PersistentDataType.STRING) != null) {
                            p.closeInventory();
                            String workerUUID = it.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"workerUUID"),PersistentDataType.STRING);
                            territoryData.showWorkerInventory(p,workerUUID,territoryData.getPlayerTerritory(p));
                        }
                    }
                }
            } else if (invView.getTitle().contains("§bGérer le villageois")) {
                e.setCancelled(true);
                if(it==null) {return;}
                switch (it.getType()) {
                    case VILLAGER_SPAWN_EGG -> {
                        //GIVE SPAWNEGG
                        ItemStack workerItem = inv.getItem(4);
                        if(workerItem != null && workerItem.getItemMeta() != null && workerItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"workerUUID"),PersistentDataType.STRING) != null) {
                            p.closeInventory();
                            String workerUUID = workerItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "workerUUID"), PersistentDataType.STRING);
                            if(!territoryData.getConfig().getBoolean("territories." + territoryData.getPlayerTerritory(p) + ".workers." + workerUUID + ".alive")){
                                ItemStack workerSpawnEgg = territoryData.getConfig().getItemStack("territories." + territoryData.getPlayerTerritory(p) + ".workers." + workerUUID + ".spawnEgg");
                                p.getInventory().addItem(workerSpawnEgg);
                                p.sendMessage(main.prefix + "§aVous avez reçu l'œuf d'apparition du villageois !");
                            } else {
                                p.sendMessage(main.prefix + "§cLe villegois est déjà en vie/activité !");
                            }
                            return;
                        } else {
                            p.sendMessage(main.prefix + "§4Une erreur s'est produite.");
                            return;
                        }
                    }
                    case IRON_SWORD -> {
                        if(e.getSlot()!=0) {return;}
                        //KILL VILLAGER
                        ItemStack workerItem = inv.getItem(4);
                        if(workerItem != null && workerItem.getItemMeta() != null && workerItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"workerUUID"),PersistentDataType.STRING) != null) {
                            p.closeInventory();
                            String workerUUID = workerItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "workerUUID"), PersistentDataType.STRING);
                            String villagerUUID = territoryData.getConfig().getString("territories." + territoryData.getPlayerTerritory(p) + ".workers." + workerUUID + ".villagerUUID");
                            boolean workerAlive = territoryData.getConfig().getBoolean("territories." + territoryData.getPlayerTerritory(p) + ".workers." + workerUUID + ".alive");
                            if(workerAlive && villagerUUID != null){
                                Villager villager = (Villager) Bukkit.getEntity(UUID.fromString(villagerUUID));
                                if(villager==null) {
                                    p.sendMessage(main.prefix + "§cVillageois non trouvé.");
                                    return;
                                }
                                villager.remove();

                                if(workerUUID == null || !territoryData.getWorkerList().contains(workerUUID)) {
                                    p.sendMessage(main.prefix + "§cLe villageois est invalide.");
                                    return;
                                }
                                String workerType = null;
                                for (String tag : villager.getScoreboardTags()) {
                                    if (tag.contains("workerType=")) {
                                        workerType = tag.replace("workerType=","");
                                    }
                                }
                                String territoryName = territoryData.getWorkerTerritory(villager);
                                territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".alive", false);
                                territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".villagerUUID", null);
                                territoryData.saveConfig();
                                if (workerType!=null) {
                                    WorkerType type = WorkerType.valueOf(workerType.toUpperCase().replace(" ", ""));
                                    if (type.getLifespan()==-1){
                                        territoryData.spawnWorker(villager,villager.getLocation());
                                        return;
                                    } else {
                                        territoryData.removeOneAliveWorkerFromCount(territoryName,type);
                                    }
                                }
                                if(territoryData.getAliveWorkerCount(territoryName,WorkerType.POLICIER) > 1) {
                                    territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été despawn par le menu du territoire !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été despawn par le menu du territoire !");
                                } else {
                                    territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué !");
                                }
                                Inventory workersTerrInv = territoryData.getTerritoryWorkersInventory(p,territoryName,1);
                                p.openInventory(workersTerrInv);
                            } else {
                                p.sendMessage(main.prefix + "§cLe villageois n'existe pas.");
                            }
                        }else {
                            p.sendMessage(main.prefix + "§4Une erreur s'est produite.");
                        }
                    }
                    case BARRIER -> p.closeInventory();
                }
            } else if (invView.getTitle().contains("§bGérer les membres")){
                e.setCancelled(true);
                if(it==null) {return;}
                if(it.getType().equals(Material.PLAYER_HEAD)) {
                    if(it.getItemMeta() == null) {
                        p.sendMessage(main.prefix + "§4Joueur non trouvé !");
                        return;
                    }
                    String uuidString = it.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"memberUUID"),PersistentDataType.STRING);
                    if(uuidString == null) {
                        p.sendMessage(main.prefix + "§4Joueur non trouvé !");
                        return;
                    }
                    UUID memberUUID = UUID.fromString(uuidString);
                    Player t = Bukkit.getPlayer(memberUUID);
                    if(t == null) {
                        p.sendMessage(main.prefix + "§4Joueur non trouvé !");
                        return;
                    }
                    String territoryName = territoryData.getPlayerTerritory(p);
                    if(!territoryData.getPlayerTerritory(t).equalsIgnoreCase(territoryName)){
                        p.sendMessage(main.prefix + "§4Le joueur n'est pas dans votre territoire");
                        return;
                    }
                    if(e.getClick().equals(ClickType.LEFT)) {
                        if(territoryData.isChief(t,territoryName)){
                            if(p.equals(t)) {
                                p.sendMessage(main.prefix + "§eVous êtes le chef de ce territoire, §cvous avez donc le grade le plus élevé.");
                            } else {
                                p.sendMessage(main.prefix + "§cVous ne pouvez pas promouvoir le chef de votre territoire, §eil a le grade le plus élevé.");
                            }
                            return;
                        }
                        if(territoryData.isOfficer(t,territoryName)) {
                            p.sendMessage(main.prefix + "§cIl ne peut que y avoir un seul chef dans un territoire !");
                            return;
                        }
                        territoryData.makeOfficer(t,p);
                        p.closeInventory();
                        territoryData.showTerritoryMembersInventory(p,territoryName,1);
                        return;
                    }
                    if(e.getClick().equals(ClickType.RIGHT)) {
                        if(territoryData.isChief(t,territoryName)){
                            if(p.equals(t)) {
                                p.sendMessage(main.prefix + "§eVous êtes le chef de ce territoire, §cvous ne pouvez pas être rétrogradé.");
                            } else {
                                p.sendMessage(main.prefix + "§cVous ne pouvez pas rétrograder le chef de votre territoire, §esinon, qui serait le chef ?");
                            }
                            return;
                        }
                        territoryData.removeOfficer(t,p);
                        p.closeInventory();
                        territoryData.showTerritoryMembersInventory(p,territoryName,1);
                        return;
                    }
                } else if(it.getType().equals(Material.END_CRYSTAL)) {
                    Inventory preInviteInv = Bukkit.createInventory(p, 54, "§bInviter un joueur au territoire");

                    preInviteInv.setItem(53, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
                    for (OfflinePlayer all : Bukkit.getOfflinePlayers()) {
                        PlayerData playerData = new PlayerData(all.getUniqueId());
                        PlayerData senderData = new PlayerData(p.getUniqueId());

                        if(playerData.getTerritory() != null || all.getUniqueId() == p.getUniqueId() || Objects.equals(playerData.getTerritory(), senderData.getTerritory())){
                            continue;
                        }
                        ItemStack playerSkull = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) playerSkull.getItemMeta();
                        skullMeta.setOwnerProfile(all.getPlayerProfile());
                        skullMeta.setLore(Collections.singletonList("§bCliquez pour inviter le joueur §5"));
                        skullMeta.setDisplayName(all.getName());
                        playerSkull.setItemMeta(skullMeta);
                        preInviteInv.addItem(playerSkull);
                    }
                    p.openInventory(preInviteInv);
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
                    p.closeInventory();
                    p.openInventory(inviteInv);
                    return;
                } else if(it.getType().equals(Material.BARRIER)) {
                    p.closeInventory();
                    return;
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
        Location loc = p.getLocation();
        Chunk chunk = loc.getChunk();
        String chunkOwner = territoryData.getChunkOwner(chunk);
        if(main.seeTerritoryBorders.contains(p)) {
            World world = loc.getWorld();
            int range = 10;
            for (int chunkX = chunk.getX() - range; chunkX < chunk.getX() + range; chunkX++) {
                for (int chunkZ = chunk.getZ() - range; chunkZ < chunk.getZ() + range; chunkZ++) {
                    assert world != null;
                    Chunk chunkToShow = world.getChunkAt(chunkX,chunkZ);
                    if (territoryData.getChunkOwner(chunkToShow) != null && !territoryData.getChunkOwner(chunkToShow).isEmpty()) {
                        ChatColor chatColor = territoryData.getTerritoryTeam(territoryData.getChunkOwner(chunkToShow)).getColor();
                        territoryData.showChunkBorder(chunkToShow, chatColor, p);
                    }
                }
            }
        }
        if(!Objects.equals(currentChunkOwner.get(p), chunkOwner)) {
            currentChunkOwner.put(p,chunkOwner);
            p.resetTitle();
            if(chunkOwner == null){
                p.sendTitle("§2§lWilderness","",20,60,20);
            } else {
                Team chunkOwnerTeam = territoryData.getTerritoryTeam(chunkOwner);
                ChatColor chunkOwnerTeamColor = chunkOwnerTeam.getColor();
                p.sendTitle(chunkOwnerTeamColor + chunkOwnerTeam.getName(), chunkOwner.equals(territoryData.getPlayerTerritory(p)) ? "§a§oVotre territoire" : "",20,60,20);
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        Material placedBlockType = e.getBlock().getType();

        try {
            PlayerData playerData = new PlayerData(p);
            String territoryName = territoryData.getPlayerTerritory(p);
            String chunkOwner = territoryData.getChunkOwner(e.getBlock().getChunk());
            if(!main.bypassClaims.contains(p) && chunkOwner != null && !chunkOwner.equals(territoryName)){
                e.setCancelled(true);
                p.sendMessage(main.prefix + "§4Vous ne pouvez pas placer de blocks ici !");
                return;
            }


            boolean isInOwnTerritory = chunkOwner != null &&
                    chunkOwner.equals(territoryName);

            int moneyGain = 1;
            int xpGain = 1;

            if(Tag.LOGS.isTagged(placedBlockType)) {
                moneyGain=moneyGain+territoryData.getAliveWorkerCount(territoryName,WorkerType.BUCHERON);
                xpGain=xpGain+territoryData.getAliveWorkerCount(territoryName,WorkerType.BUCHERON);
                playerData.addMoney(moneyGain);
                moneyGained.put(playerId, moneyGained.getOrDefault(playerId, 0) + moneyGain);
                if (isInOwnTerritory) {
                    territoryData.addTerritoryXP(territoryData.getPlayerTerritory(p), xpGain);
                    xpGained.put(playerId, xpGained.getOrDefault(playerId, 0) + xpGain);
                }
            } else if(CustomPlantTag.isCustomPlant(placedBlockType)) {
                moneyGain=moneyGain+territoryData.getAliveWorkerCount(territoryName,WorkerType.JARDINIER);
                xpGain=xpGain+territoryData.getAliveWorkerCount(territoryName,WorkerType.JARDINIER);
                playerData.addMoney(moneyGain);
                moneyGained.put(playerId, moneyGained.getOrDefault(playerId, 0) + moneyGain);
                if (isInOwnTerritory) {
                    territoryData.addTerritoryXP(territoryData.getPlayerTerritory(p), xpGain);
                    xpGained.put(playerId, xpGained.getOrDefault(playerId, 0) + xpGain);
                }
            } else {
                playerData.addMoney(moneyGain);
                moneyGained.put(playerId, moneyGained.getOrDefault(playerId, 0) + moneyGain);
                if (isInOwnTerritory) {
                    territoryData.addTerritoryXP(territoryData.getPlayerTerritory(p), xpGain);
                    xpGained.put(playerId, xpGained.getOrDefault(playerId, 0) + xpGain);
                }
            }

            // Build and send the action bar message
            BaseComponent baseComponent = new ComponentBuilder()
                    .append("[").color(net.md_5.bungee.api.ChatColor.GRAY)
                    .append("Argent").color(net.md_5.bungee.api.ChatColor.DARK_GREEN)
                    .append("] ").color(net.md_5.bungee.api.ChatColor.GRAY)
                    .append("+").color(net.md_5.bungee.api.ChatColor.WHITE)
                    .append(String.valueOf(moneyGained.get(playerId))).color(net.md_5.bungee.api.ChatColor.GREEN)
                    .append(main.moneySign).color(net.md_5.bungee.api.ChatColor.GREEN)
                    .append(isInOwnTerritory ? " & " : "").color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .append(isInOwnTerritory ? "[" : "").color(net.md_5.bungee.api.ChatColor.GRAY)
                    .append(isInOwnTerritory ? "XP " : "").color(net.md_5.bungee.api.ChatColor.GREEN)
                    .append(isInOwnTerritory ? "Territoire" : "").color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE)
                    .append(isInOwnTerritory ? "] " : "").color(net.md_5.bungee.api.ChatColor.GRAY)
                    .append(isInOwnTerritory ? "+" : "").color(net.md_5.bungee.api.ChatColor.WHITE)
                    .append(isInOwnTerritory ? xpGained.get(playerId) + " XP" : "").color(net.md_5.bungee.api.ChatColor.GREEN)
                    .build();

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponent);

            // Cancel the old reset task if it exists
            if (resetTasks.containsKey(playerId)) {
                resetTasks.get(playerId).cancel();
            }

            // Schedule a new reset task after 5 seconds of inactivity
            BukkitTask resetTask = new BukkitRunnable() {
                @Override
                public void run() {
                    moneyGained.remove(playerId);
                    xpGained.remove(playerId);
                    resetTasks.remove(playerId);
                }
            }.runTaskLater(main, 60L); // 60 ticks = 3 seconds

            resetTasks.put(playerId, resetTask);

        } catch (Exception ex) {
            main.logError("Couldn't give reward to player for placing block", ex);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getClickedBlock() != null) {
            String territoryName = territoryData.getPlayerTerritory(p);
            String chunkOwner = territoryData.getChunkOwner(e.getClickedBlock().getChunk());
            if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if(!main.bypassClaims.contains(p) && chunkOwner != null && !chunkOwner.equals(territoryName)){
                    e.setCancelled(true);
                    p.sendMessage(main.prefix + "§4Vous ne pouvez pas casser avec des blocs ici !");
                    return;
                }
            }
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                if(!main.bypassClaims.contains(p) && chunkOwner != null && !chunkOwner.equals(territoryName)){
                    e.setCancelled(true);
                    p.sendMessage(main.prefix + "§4Vous ne pouvez pas placer avec des blocs/intéragir ici !");
                    return;
                }
            }
        }

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
    public void onBlockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        String territoryName = territoryData.getPlayerTerritory(p);
        String chunkOwner = territoryData.getChunkOwner(e.getBlock().getChunk());
        if(!main.bypassClaims.contains(p) && chunkOwner != null && !chunkOwner.equals(territoryName)){
            e.setCancelled(true);
            p.sendMessage(main.prefix + "§4Vous ne pouvez pas casser de blocs ici !");
            return;
        }
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player p) {
            String territoryName = territoryData.getPlayerTerritory(p);
            String chunkOwner = territoryData.getChunkOwner(e.getEntity().getLocation().getChunk());
            if(!main.bypassClaims.contains(p) && chunkOwner != null && !chunkOwner.equals(territoryName)){
                e.setCancelled(true);
                if(e.getEntity() instanceof Player) {
                    p.sendMessage(main.prefix + "§4Vous ne pouvez pas attaquer d'autres joueurs ici !");
                } else {
                    p.sendMessage(main.prefix + "§4Vous ne pouvez pas attaquer d'entitées ici !");
                }
            }
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
            String territoryName = territoryData.getWorkerTerritory(villager);
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".alive", false);
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".villagerUUID", null);
            territoryData.saveConfig();
            if (workerType!=null) {
                WorkerType type = WorkerType.valueOf(workerType.toUpperCase().replace(" ", "_"));
                int tier = territoryData.getConfig().getInt("territories." + territoryName + ".workers." + workerUUID + ".tier");
                for (String tag : villager.getScoreboardTags()) {
                    if (tag.contains("tier=")) {
                        tier = Integer.parseInt(tag.replace("tier=",""));
                    }
                }

                if (type.getLifespan()==-1){
                    territoryData.spawnWorker(villager,e.getEntity().getLocation());
                    return;
                } else {
                    territoryData.removeAliveWorkerFromCount(territoryName,type, 1 + tier);
                }
            }
            if(territoryData.getAliveWorkerCount(territoryName,WorkerType.POLICIER) > 1) {
                try {
                    if(e.getDamageSource().getCausingEntity()!=null) {
                        Entity damager = e.getDamageSource().getCausingEntity();
                        if(damager instanceof Player p) {
                            territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué par §c" + p.getName() + "§4 !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué par §c" + p.getName() + " §4!");

                        } else {
                            territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué par un/une §c" + damager.getType() + "§4 !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué par un/une §c" + damager.getType() + " §4!");
                        }
                        return;
                    } else if (e.getDamageSource().getDamageType() != null) {
                        String damageTypeMessage = getDamageType(e);
                        if(damageTypeMessage==null) {
                            territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué !");
                            return;
                        }
                        if(damageTypeMessage.equals("COMMAND")) {
                            territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé à été despawn par le menu des villageois !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été despawn par le menu des villageois !");
                            return;
                        }
                        territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé est mort §c" + damageTypeMessage + "§4 !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4est mort §c" + damageTypeMessage + " §4!");
                        return;
                    } else {
                        territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué !");
                        return;
                    }
                } catch (Exception ex) {
                    main.logError("Couldn't find out worker's death reason",ex);
                    territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué !");
                    return;
                }
            } else {
                territoryData.sendAnouncementToTerritory(territoryName,workerType==null ? "§4Un employé a été tué !" : "§4Un employé de type §e" + territoryData.formatType(workerType) + " §4a été tué !");
                return;
            }
        }
    }

    private String getDamageType(EntityDeathEvent e) {
        DamageType damageType  = e.getDamageSource().getDamageType();
        if(damageType.equals(DamageType.FALL)) {
            return "de dégats de chute";
        } else if(damageType.equals(DamageType.CAMPFIRE)) {
            return "d'un feu de camp'";
        } else if(damageType.equals(DamageType.CACTUS)) {
            return "d'un cactus";
        } else if(damageType.equals(DamageType.CRAMMING)) {
            return "d'Entity Cramming";
        }  else if(damageType.equals(DamageType.DROWN)) {
            return "de noyade";
        } else if(damageType.equals(DamageType.EXPLOSION)) {
            return "à cause d'une explosion";
        } else if(damageType.equals(DamageType.FALLING_ANVIL)) {
            return "à cause d'une enclume";
        } else if(damageType.equals(DamageType.FALLING_BLOCK)) {
            return "de suffocation";
        } else if(damageType.equals(DamageType.FALLING_STALACTITE)) {
            return "à cause d'une stalactite";
        } else if(damageType.equals(DamageType.FIREBALL)) {
            return "à cause d'une boule de feu";
        } else if(damageType.equals(DamageType.FREEZE)) {
            return "de froid";
        } else if(damageType.equals(DamageType.ON_FIRE)) {
            return "de feu";
        } else if(damageType.equals(DamageType.OUT_OF_WORLD)) {
            return "d'une chute dans le vide";
        } else if(damageType.equals(DamageType.MAGIC)) {
            return "d'une potion";
        } else if(damageType.equals(DamageType.FIREWORKS)) {
            return "à cause de feux d'artifice";
        } else if(damageType.equals(DamageType.LAVA)) {
            return "car il a essayé de nager dans de la lave";
        } else if(damageType.equals(DamageType.THORNS)) {
            return "à cause de l'enchantement Épines";
        } else if(damageType.equals(DamageType.WIND_CHARGE)) {
            return "d'une charge de vent";
        } else if(damageType.equals(DamageType.HOT_FLOOR)) {
            return "à cause de magma";
        } else if(damageType.equals(DamageType.SWEET_BERRY_BUSH)) {
            return "à cause d'un buisson de Sweet Berries";
        } else if(damageType.equals(DamageType.LIGHTNING_BOLT)) {
            return "à cause d'un éclair...§6 c'est quoi la proba??";
        } else if(damageType.equals(DamageType.GENERIC)) {
            return "COMMAND";
        } else if(damageType.equals(DamageType.GENERIC_KILL)) {
            return "COMMAND";
        } else {
            return null;
        }
    }
}
