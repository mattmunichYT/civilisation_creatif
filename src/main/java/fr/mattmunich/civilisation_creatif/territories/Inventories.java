package fr.mattmunich.civilisation_creatif.territories;

import fr.mattmunich.civilisation_creatif.helpers.ItemBuilder;
import fr.mattmunich.civilisation_creatif.helpers.PlayerData;
import fr.mattmunich.civilisation_creatif.helpers.WorkerType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inventories {
    private final TerritoryData territoryData;

    public Inventories(TerritoryData territoryData) {
        this.territoryData = territoryData;
    }

    public Inventory getTerrListInv_Layout(Player p, int page, int pageNum) {
        Inventory terrListInv = Bukkit.createInventory(p, 54, "§aListe des territoires §7- §ePage §6" + page);
        ItemStack none = ItemBuilder.getItem(Material.WHITE_STAINED_GLASS_PANE, "");
        for (int i = 0; i < 53; i++) {
            terrListInv.setItem(i, none);
        }
        //Borders
        terrListInv.setItem(0, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(1, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(7, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(8, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(9, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(17, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(36, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(44, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(45, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(46, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(52, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(53, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        //Borders
        //Inner part
        int[] ranges = {10, 16, 19, 25, 28, 34, 37, 43};

        for (int i = 0; i < ranges.length; i += 2) {
            for (int slot = ranges[i]; slot <= ranges[i + 1]; slot++) {
                terrListInv.setItem(slot, null);
            }
        }
        //Inner part
        //Navigation bar
        if (page != 1) {
            terrListInv.setItem(47, ItemBuilder.getItem(Material.RED_STAINED_GLASS_PANE, "§c§l←", false, false, null, null, null));
        }
        terrListInv.setItem(49, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
        if (page != pageNum) {
            terrListInv.setItem(51, ItemBuilder.getItem(Material.LIME_STAINED_GLASS_PANE, "§a§l→", false, false, null, null, null));
        }
        //Navigation bar
        return terrListInv;
    }

    public int extractInventoryPageNumber(String title) {
        Pattern pattern = Pattern.compile("§6(\\d+)$");
        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return 1;
        }
    }

    public Inventory getTerritoryListInventory(Player p, int page) {
        int itemsPerPage = 28; // Number of items per page
        List<String> territoriesList = territoryData.getTerritoriesList();
        int totalPages = (int) Math.ceil((double) territoriesList.size() / itemsPerPage);

        // Ensure the page number is within valid range
        if (page < 1 || page > totalPages) {
            page = 1; // Default to page 1 if out of bounds
        }

        // Create the inventory for the specific page
        Inventory terrListInv_Layout = getTerrListInv_Layout(p, page, totalPages);

        // Calculate start and end index for the items on this page
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, territoriesList.size());

        // Populate the inventory with items for the current page
        for (int i = startIndex; i < endIndex; i++) {
            String terr = territoriesList.get(i);
            Team territory = territoryData.getTerritoryTeam(terr);
            if (territory == null) {
                continue;
            }
            String chiefName;
            try {
                OfflinePlayer chief = Bukkit.getOfflinePlayer(UUID.fromString(territoryData.getTerritoryChiefUUID(terr)));
                chiefName = chief.getName();
            } catch (NullPointerException e) {
                chiefName = "§c§oNon trouvé";
            }

            String descriptionString = territoryData.getTerritoryDescription(terr);
            List<String> desc = splitDescription(descriptionString, 30);
            List<String> lore = new ArrayList<>(Arrays.asList(
                    "§2Chef: §a" + chiefName,
                    "§2Officiers: §a" + territoryData.getTerritoryOfficers(terr).size(),
                    "§2Membres: §a" + territoryData.getTerritoryMembersUUID(terr).size(),
                    "§2XP:§a " + territoryData.getTerritoryXP(terr),
                    "§2Argent:§a " + territoryData.getTerritoryMoney(terr),
                    "",
                    "§2Description:§a"
            ));

            if (desc == null) {
                lore.add("§8§oNon définie");
            } else {
                lore.addAll(desc);
            }

            lore.add("");


            ItemStack banner = territoryData.getTerritoryBanner(terr);
            ItemMeta bannerMeta = banner.getItemMeta();
            assert bannerMeta != null;
            bannerMeta.setDisplayName(territory.getColor() + territory.getName());
            bannerMeta.setLore(lore);
            banner.setItemMeta(bannerMeta);

            // Add the item to the next available slot
            terrListInv_Layout.addItem(banner);
        }

        // Return the populated inventory for the specified page
        return terrListInv_Layout;
    }

    public @Nullable List<String> splitDescription(String description, int maxLength) {
        if (description == null) {
            return null;
        }
        List<String> lines = new ArrayList<String>();
        String[] words = description.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    public Inventory getTerrInv(Player p, Team territory) {
        Inventory terrInv = Bukkit.createInventory(p, 27, "§aTerritoire : " + territory.getColor() + territory.getName());
        ItemStack none = ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null, null, null);
        for (int i = 0; i < 26; i++) {
            terrInv.setItem(i, none);
        }
        String terr = territory.getName();
        String chiefName;
        try {
            OfflinePlayer chief = Bukkit.getOfflinePlayer(UUID.fromString(territoryData.getTerritoryChiefUUID(terr)));
            chiefName = chief.getName();
        } catch (NullPointerException e) {
            chiefName = "§c§oNon trouvé";
        }
        String descriptionString = territoryData.getTerritoryDescription(terr);
        List<String> desc = splitDescription(descriptionString, 30);
        List<String> lore = new ArrayList<String>(Arrays.asList(
                "§2Chef: §a" + chiefName,
                "§2Officiers: §a" + territoryData.getTerritoryOfficers(terr).size(),
                "§2Membres: §a" + territoryData.getTerritoryMembersUUID(terr).size(),
                "§2XP:§a " + territoryData.getTerritoryXP(terr),
                "§2Argent:§a " + territoryData.getTerritoryMoney(terr),
                "",
                "§2Description:§a"
        ));

        if (desc == null) {
            lore.add("§8§oNon définie");
        } else {
            lore.addAll(desc);
        }

        lore.add("");


        ItemStack banner = territoryData.getTerritoryBanner(terr);
        BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
        assert bannerMeta != null;
        if (territoryData.hasTerritory(p) && (territoryData.isChief(p, terr))) {
            bannerMeta.setItemName("§r§dDéfinir la bannière du territoire");
        } else {
            bannerMeta.setItemName(territory.getColor() + territory.getName());
        }
        bannerMeta.setLore(lore);
        banner.setItemMeta(bannerMeta);
        terrInv.setItem(4, banner);
        terrInv.setItem(13, ItemBuilder.getItem(Material.PAPER, "§a§oℹ Menu du territoire " + territory.getColor() + territory.getName(), lore));

        if (territoryData.hasTerritory(p) && (territoryData.isOfficer(p, terr) || territoryData.isChief(p, terr))) {
            terrInv.setItem(9, ItemBuilder.getItem(Material.VILLAGER_SPAWN_EGG, "§b\uD83D\uDEE0✎ Gérer les villageois"));
            terrInv.setItem(12, ItemBuilder.getItem(Material.END_CRYSTAL, "§b👤➕ Inviter des joueurs"));
            terrInv.setItem(17, ItemBuilder.getItem(Material.PLAYER_HEAD, "§b👤✎ Gérer les membres"));
        }
        if (territoryData.hasTerritory(p) && (territoryData.isChief(p, terr))) {
            terrInv.setItem(3, ItemBuilder.getItem(Material.WRITABLE_BOOK, "§a✎ Changer §5la description§a de votre territoire"));
            terrInv.setItem(5, ItemBuilder.getItem(Material.OAK_SIGN, "§2✎ Changer §5le nom§2 de votre territoire"));
            terrInv.setItem(14, ItemBuilder.getItem(Material.CYAN_STAINED_GLASS, "§3Changer la couleur de votre territoire"));
            terrInv.setItem(22, ItemBuilder.getItem(Material.RED_DYE, "§4❌ Supprimer le territoire"));
        }
        terrInv.setItem(26, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu"));
        return terrInv;
    }

    public Inventory getTerrMemebersInv_Layout(Player p, int page, int pageNum) {
        Inventory terrListInv = Bukkit.createInventory(p, 54, "§bGérer les membres §7- §ePage §6" + page);
        ItemStack none = ItemBuilder.getItem(Material.WHITE_STAINED_GLASS_PANE, null, false, false, null, null, null);
        for (int i = 0; i < 53; i++) {
            terrListInv.setItem(i, none);
        }
        //Borders
        terrListInv.setItem(0, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(1, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(7, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(8, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(9, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(17, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(36, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(44, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(45, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(46, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(52, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(53, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        //Borders
        //Inner part
        int[] ranges = {10, 16, 19, 25, 28, 34, 37, 43};

        for (int i = 0; i < ranges.length; i += 2) {
            for (int slot = ranges[i]; slot <= ranges[i + 1]; slot++) {
                terrListInv.setItem(slot, null);
            }
        }
        //Inner part
        //Navigation bar
        if (page != 1) {
            terrListInv.setItem(47, ItemBuilder.getItem(Material.RED_STAINED_GLASS_PANE, "§c§l←", false, false, null, null, null));
        }
        terrListInv.setItem(49, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
        if (page != pageNum) {
            terrListInv.setItem(51, ItemBuilder.getItem(Material.LIME_STAINED_GLASS_PANE, "§a§l→", false, false, null, null, null));
        }
        //Navigation bar
        return terrListInv;
    }

    public void showTerritoryMembersInventory(Player p, String territoryName, int page) {
        int itemsPerPage = 28;
        List<String> memberList = territoryData.getTerritoryMembersUUID(territoryName);
        int totalPages = (int) Math.ceil((double) memberList.size() / itemsPerPage);

        if (page < 1 || page > totalPages) {
            page = 1;
        }

        Inventory terrMembersInv = getTerrMemebersInv_Layout(p, page, totalPages);

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, memberList.size());

        //LOAD INV WITHOUT PLAYER SKIN ON SKULL
        for (int i = startIndex; i < endIndex; i++) {
            String memberUUID = memberList.get(i);
            OfflinePlayer member = Bukkit.getOfflinePlayer(UUID.fromString(memberUUID));
            if (!member.hasPlayedBefore()) {
                Bukkit.getLogger().warning("Removed member " + memberUUID + " in territory " + territoryName + " from terrMember menu because it has never played before");
                continue;
            }
            boolean memberOnline = member.isOnline();
            boolean isChief = territoryData.isChief(member, territoryName);
            boolean isOfficer = territoryData.isOfficer(member, territoryName);

            String rankString = "§aGrade : " + (isChief ? "§6§lChef" : (isOfficer ? "§2Officier" : "§7Membre"));
            String onlineString = "§aEn ligne : " + (memberOnline ? "§2§lOui" : "§cNon");
            String tipPromote = (!isChief ? "§7§oCLIC GAUCHE pour promouvoir à " + (isOfficer ? "§6§lChef" : "§2Officier") : null);
            String tipDemote = (isChief || isOfficer ? "§7§oCLIC DROIT pour rétrograder à " + (isChief ? "§2Officier" : "§7Membre") : null);

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta playerHeadMeta = (SkullMeta) playerHead.getItemMeta();
            assert playerHeadMeta != null;
            PersistentDataContainer data = playerHeadMeta.getPersistentDataContainer();
            data.set(new NamespacedKey(territoryData.getPlugin(), "memberUUID"), PersistentDataType.STRING, member.getUniqueId().toString());
            playerHeadMeta.setDisplayName(p == member ? member.getName() + "§8 - §a§aVous" : member.getName());
            playerHeadMeta.setOwnerProfile(member.getPlayerProfile());
            playerHeadMeta.setLore(Arrays.asList(rankString, onlineString, "", tipPromote, tipDemote));
            playerHead.setItemMeta(playerHeadMeta);
            terrMembersInv.addItem(playerHead);
        }
        terrMembersInv.setItem(0, ItemBuilder.getItem(Material.PAPER, "§bℹ Informations",
                Arrays.asList(
                        "    §bIci, vous pouvez gérer les",
                        "    §bmembres de votre territoire :",
                        "    §aCLIC GAUCHE : promouvoir le membre",
                        "    §aCLIC DROIT : rétrograder le membre"
                )));
        terrMembersInv.setItem(4, ItemBuilder.getItem(Material.SUNFLOWER, "§bℹ Les membres de votre territoire",
                List.of(
                        "§aNombre de membres : §6" + memberList.size()
                )
        ));
        terrMembersInv.setItem(8, ItemBuilder.getItem(Material.END_CRYSTAL, "§bInviter des joueurs"));
        p.openInventory(terrMembersInv);

        //LOAD INV WITH PLAYER SKIN ON SKULL
        terrMembersInv = getTerrMemebersInv_Layout(p, page, totalPages);

        // Populate the inventory with items for the current page
        for (int i = startIndex; i < endIndex; i++) {
            String memberUUID = memberList.get(i);
            OfflinePlayer member = Bukkit.getOfflinePlayer(UUID.fromString(memberUUID));
            if (!member.hasPlayedBefore()) {
                Bukkit.getLogger().warning("Removed member " + memberUUID + " in territory " + territoryName + " from terrMember menu because it has never played before");
                continue;
            }
            boolean memberOnline = member.isOnline();
            boolean isChief = territoryData.isChief(member, territoryName);
            boolean isOfficer = territoryData.isOfficer(member, territoryName);
            PlayerData playerData = new PlayerData(p);

            String rankString = "§aGrade : " + (isChief ? "§6§lChef" : (isOfficer ? "§2Officier" : "§7Membre"));
            String onlineString = "§aEn ligne : " + (memberOnline ? "§2§lOui" : "§cNon");
            String tipPromote = (!isChief ? "§7§oCLIC GAUCHE pour promouvoir à " + (isOfficer ? "§6§lChef" : "§2Officier") : null);
            String tipDemote = (isOfficer ? "§7§oCLIC DROIT pour rétrograder à " + (isChief ? "§2Officier" : "§7Membre") : null);

            ItemStack playerHead = playerData.getSkull(p);
            SkullMeta playerHeadMeta = (SkullMeta) playerHead.getItemMeta();
            assert playerHeadMeta != null;
            PersistentDataContainer data = playerHeadMeta.getPersistentDataContainer();
            data.set(new NamespacedKey(territoryData.getPlugin(), "memberUUID"), PersistentDataType.STRING, member.getUniqueId().toString());
            playerHeadMeta.setDisplayName(member.getName());
            playerHeadMeta.setOwnerProfile(member.getPlayerProfile());
            playerHeadMeta.setLore(Arrays.asList(rankString, onlineString, "", tipPromote, tipDemote));
            playerHead.setItemMeta(playerHeadMeta);
            terrMembersInv.addItem(playerHead);
        }
        terrMembersInv.setItem(0, ItemBuilder.getItem(Material.PAPER, "§bℹ Informations",
                Arrays.asList(
                        "    §bIci, vous pouvez gérer les",
                        "    §bmembres de votre territoire :",
                        "    §aCLIC GAUCHE : promouvoir le membre",
                        "    §aCLIC DROIT : rétrograder le membre"
                )
        ));
        terrMembersInv.setItem(4, ItemBuilder.getItem(Material.SUNFLOWER, "§bℹ Les membres de votre territoire",
                List.of(
                        "§aNombre de membres : §6" + memberList.size()
                )
        ));
        terrMembersInv.setItem(8, ItemBuilder.getItem(Material.END_CRYSTAL, "§bInviter des joueurs"));
        p.openInventory(terrMembersInv);
    }

    public Inventory getTerrWorkersInv_Layout(Player p, int page, int pageNum) {
        Inventory terrListInv = Bukkit.createInventory(p, 54, "§bGérer vos villageois §7- §ePage §6" + page);
        ItemStack none = ItemBuilder.getItem(Material.WHITE_STAINED_GLASS_PANE, null, false, false, null, null, null);
        for (int i = 0; i < 53; i++) {
            terrListInv.setItem(i, none);
        }
        //Borders
        terrListInv.setItem(0, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(1, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(7, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(8, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(9, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(17, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(36, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(44, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(45, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(46, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(52, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        terrListInv.setItem(53, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        //Borders
        //Inner part
        int[] ranges = {10, 16, 19, 25, 28, 34, 37, 43};

        for (int i = 0; i < ranges.length; i += 2) {
            for (int slot = ranges[i]; slot <= ranges[i + 1]; slot++) {
                terrListInv.setItem(slot, null);
            }
        }
        //Inner part
        //Navigation bar
        if (page != 1) {
            terrListInv.setItem(47, ItemBuilder.getItem(Material.RED_STAINED_GLASS_PANE, "§c§l←", false, false, null, null, null));
        }
        terrListInv.setItem(49, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
        if (page != pageNum) {
            terrListInv.setItem(51, ItemBuilder.getItem(Material.LIME_STAINED_GLASS_PANE, "§a§l→", false, false, null, null, null));
        }
        //Navigation bar
        return terrListInv;
    }

    public Inventory getTerritoryWorkersInventory(Player p, String territoryName, int page) {
        int itemsPerPage = 28;
        List<String> workerList = territoryData.getTerritoryWorkerList(territoryName);
        int totalPages = (int) Math.ceil((double) workerList.size() / itemsPerPage);

        if (page < 1 || page > totalPages) {
            page = 1;
        }

        Inventory terrWorkersInv = getTerrWorkersInv_Layout(p, page, totalPages);

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, workerList.size());

        // Populate the inventory with items for the current page
        for (int i = startIndex; i < endIndex; i++) {
            String workerUUID = workerList.get(i);
            String pathToWorker = "territories." + territoryName + ".workers." + workerUUID;
            boolean workerAlive = territoryData.getConfig().getBoolean(pathToWorker + ".alive");
            int tier = territoryData.getConfig().getInt(pathToWorker + ".tier");
            WorkerType workerType = WorkerType.valueOf(Objects.requireNonNull(territoryData.getConfig().getString(pathToWorker + ".type")).toUpperCase());
            int daysToLive = territoryData.getConfig().getInt(pathToWorker + ".daysToLive");
            int daysLived = territoryData.getConfig().getInt(pathToWorker + ".daysLived") + 1;
            int income = workerType.getIncome();
            ChatColor tierColor = ChatColor.DARK_GRAY;
            switch (tier) {
                case 0:
                    break;
                case 1:
                    tierColor = ChatColor.GRAY;
                    income = (int) (income + (income * 0.1));//+10%
                    break;
                case 2:
                    tierColor = ChatColor.YELLOW;
                    income = (int) (income + (income * 0.25));//+25%
                    break;
                case 3:
                    tierColor = ChatColor.GREEN;
                    income = (int) (income + (income * 0.45));//+45%
                    break;
                case 4:
                    tierColor = ChatColor.AQUA;
                    income = (int) (income + (income * 0.70));//+70%
                    break;
                case 5:
                    tierColor = ChatColor.BLACK;
                    income = (int) (income + (income * 0.95));//+95%
                    break;
            }

            Material workerItemType = workerType.getItem();
            String typeName = territoryData.formatType(workerType.toString());
            String workerAliveString = "§aEn vie/activité : " + (workerAlive ? "§2Oui" : "§cNon");
            String incomeString = "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois";
            String lifespan = "§aDurée de vie restante : " + (workerType.getLifespan() == -1 ? "§b§oInvincible" : (daysToLive < 10 ? "§4" : daysToLive < 30 ? "§c" : daysToLive < 45 ? "§e" : daysToLive < 90 ? "§6" : "§1") + daysToLive + "§a jours");
            String tierString = "§aTier : §6" + tier;

            ItemStack workerItem = new ItemStack(workerItemType);
            ItemMeta workerItemMeta = workerItem.getItemMeta();
            assert workerItemMeta != null;
            PersistentDataContainer data = workerItemMeta.getPersistentDataContainer();
            data.set(new NamespacedKey(territoryData.getPlugin(), "workerUUID"), PersistentDataType.STRING, workerUUID);
            workerItemMeta.setDisplayName(tierColor + typeName);
            workerItemMeta.setLore(Arrays.asList(workerAliveString, incomeString, lifespan, tierString));
            workerItem.setItemMeta(workerItemMeta);
            terrWorkersInv.addItem(workerItem);
        }
        terrWorkersInv.setItem(0, ItemBuilder.getItem(Material.PAPER, "§bℹ Informations",
                Arrays.asList(
                        "    §bIci, vous pouvez gérer les",
                        "    §bvillageois de votre territoire",
                        "§aℹ Les villageois vous rapportent",
                        "    §aune somme d'argent définie",
                        "    §achaque mois. Certains ont",
                        "    §amême d'autres utilités..."
                )));
        terrWorkersInv.setItem(4, ItemBuilder.getItem(Material.SUNFLOWER, "§bℹ Vos villageois",
                List.of(
                        "§aNombre de villageois actifs : §6" + territoryData.getTotalAliveWorkerCount(territoryName)
                )));
        terrWorkersInv.setItem(8, ItemBuilder.getItem(Material.VILLAGER_SPAWN_EGG, "§a💰 Acheter des villageois"));
        return terrWorkersInv;
    }

    public Inventory hasNoTerritory_Menu(Player p) {
        Inventory hasNoTerrMenu = Bukkit.createInventory(p, 27, "§6Menu §7- §a/territoire §7(§8§oAucun§7)");
        for (int i = 0; i <= 26; i++) {
            ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta paneMeta = pane.getItemMeta();
            assert paneMeta != null;
            paneMeta.setItemName("");
            paneMeta.setHideTooltip(true);
            pane.setItemMeta(paneMeta);
            hasNoTerrMenu.setItem(i, pane);
        }
        ItemStack joinItem = new ItemStack(Material.END_CRYSTAL);
        ItemMeta joinMeta = joinItem.getItemMeta();
        assert joinMeta != null;
        joinMeta.setItemName("§b\uD83D\uDC64➕ Rejoindre un territoire");
        joinItem.setItemMeta(joinMeta);
        hasNoTerrMenu.setItem(14, joinItem);

        ItemStack createTerrItem = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta createTerrMeta = createTerrItem.getItemMeta();
        assert createTerrMeta != null;
        createTerrMeta.setItemName("§2➕ Créer son territoire");
        createTerrItem.setItemMeta(createTerrMeta);
        hasNoTerrMenu.setItem(13, createTerrItem);

        ItemStack viewTerrsItem = new ItemStack(Material.SPYGLASS);
        ItemMeta viewTerrsMeta = viewTerrsItem.getItemMeta();
        assert viewTerrsMeta != null;
        viewTerrsMeta.setItemName("§5\uD83D\uDD0E Voir la liste des territoires");
        viewTerrsItem.setItemMeta(viewTerrsMeta);
        hasNoTerrMenu.setItem(12, viewTerrsItem);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeItemMeta = closeItem.getItemMeta();
        assert closeItemMeta != null;
        closeItemMeta.setItemName("§4❌ Fermer le menu");
        closeItem.setItemMeta(closeItemMeta);
        hasNoTerrMenu.setItem(26, closeItem);
        return hasNoTerrMenu;
    }

    public Inventory hasTerritory_Menu(Player p) {
        Inventory hasTerrMenu = Bukkit.createInventory(p, 27, "§6Menu §7- §a/territoire §7(" + territoryData.getTerritoryTeamOfPlayer(p).getColor() + territoryData.getTerritoryTeamOfPlayer(p).getName() + "§7)");
        for (int i = 0; i <= 26; i++) {
            ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta paneMeta = pane.getItemMeta();
            assert paneMeta != null;
            paneMeta.setHideTooltip(true);
            pane.setItemMeta(paneMeta);
            hasTerrMenu.setItem(i, pane);
        }
        ItemStack quitTerrItem = new ItemStack(Material.RED_DYE);
        ItemMeta quitTerrMeta = quitTerrItem.getItemMeta();
        assert quitTerrMeta != null;
        quitTerrMeta.setItemName("§c\uD83D\uDC64❌ Quitter son territoire");
        quitTerrItem.setItemMeta(quitTerrMeta);
        hasTerrMenu.setItem(14, quitTerrItem);

        ItemStack modifyTerrItem = new ItemStack(Material.PAPER);
        ItemMeta modifyTerrMeta = modifyTerrItem.getItemMeta();
        assert modifyTerrMeta != null;
        modifyTerrMeta.setItemName("§e✎ Voir son territoire");
        modifyTerrItem.setItemMeta(modifyTerrMeta);
        hasTerrMenu.setItem(13, modifyTerrItem);

        ItemStack viewTerrsItem2 = new ItemStack(Material.SPYGLASS);
        ItemMeta viewTerrsMeta2 = viewTerrsItem2.getItemMeta();
        assert viewTerrsMeta2 != null;
        viewTerrsMeta2.setItemName("§5\uD83D\uDD0E Voir la liste des territoires");
        viewTerrsItem2.setItemMeta(viewTerrsMeta2);
        hasTerrMenu.setItem(12, viewTerrsItem2);

        ItemStack closeItem2 = new ItemStack(Material.BARRIER);
        ItemMeta closeItemMeta2 = closeItem2.getItemMeta();
        assert closeItemMeta2 != null;
        closeItemMeta2.setItemName("§4❌ Fermer le menu");
        closeItem2.setItemMeta(closeItemMeta2);
        hasTerrMenu.setItem(26, closeItem2);
        return hasTerrMenu;
    }
}