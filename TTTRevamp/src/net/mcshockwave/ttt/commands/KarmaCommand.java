package net.mcshockwave.ttt.commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.ttt.KarmaManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KarmaCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD
					+ KarmaManager.getKarma(sender.getName(), false) + ChatColor.GREEN + " karma.");
		}
		if (args.length == 3
				&& (sender instanceof Player && SQLTable.hasRank(sender.getName(), Rank.MOD) || !(sender instanceof Player)
						&& sender.isOp())) {
			if (args[0].equalsIgnoreCase("set")) {
				if (Bukkit.getPlayer(args[1]) != null) {
					SQLTable.Karma.set("Karma", args[2], "Username", args[1]);
					sender.sendMessage("§cSet karma for " + args[1] + " to " + args[2]);
				}
			}
			if (args[0].equalsIgnoreCase("add")) {
				if (Bukkit.getPlayer(args[1]) != null) {
					SQLTable.Karma.set("Karma",
							(SQLTable.Karma.getInt("Username", args[1], "Karma") + Integer.parseInt(args[2])) + "",
							"Username", args[1]);
					sender.sendMessage("§cAdded " + args[2] + " karma to " + args[1]);
				}
			}
		}
		return false;
	}

}
