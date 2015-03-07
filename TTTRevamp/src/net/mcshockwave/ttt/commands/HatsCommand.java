package net.mcshockwave.ttt.commands;

import net.mcshockwave.ttt.cosmetics.Hats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HatsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			Hats.getMenu(p).open(p);
		}
		return false;
	}

}
