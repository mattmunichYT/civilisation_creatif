package fr.mattmunich.civilisation_creatif.commands;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MJEPCommand implements CommandExecutor {

    private Main main;

    public MJEPCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
        if(!(s instanceof Player)) {
            s.sendMessage(main.playerToExc);
            return true;
        }

        Player p = (Player)s;
        p.sendMessage(main.prefix + "Transfert en cours...");
        p.transfer("91.197.6.60",25599);
        return true;
    }
}
