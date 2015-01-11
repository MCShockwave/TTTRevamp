package net.mcshockwave.ttt.commands;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.ttt.GameWorlds;
import net.mcshockwave.ttt.GameWorlds.GameMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TTTCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (args[0].equalsIgnoreCase("world")) {
				World w = Bukkit.getWorld(args[1]);
				p.teleport(w.getSpawnLocation());
			}
			if (args[0].equalsIgnoreCase("updateMap")) {
				GameWorlds.updateMap(args[1]);
			}
			if (args[0].equalsIgnoreCase("updateAll")) {
				for (GameMap map : GameWorlds.mapList) {
					GameWorlds.updateMap(map.world);
				}
			}
			if (args[0].equalsIgnoreCase("listMaps")) {
				for (GameMap g : GameWorlds.mapList) {
					MCShockwave.send(ChatColor.GOLD, p, "%s: %s by %s", g.world, g.name, g.author);
				}
			}
			if (args[0].equalsIgnoreCase("updateMapList")) {
				GameWorlds.updateMapList();
				p.sendMessage("§cUpdated maps for all games");
			}
		}

		return true;
	}

}
