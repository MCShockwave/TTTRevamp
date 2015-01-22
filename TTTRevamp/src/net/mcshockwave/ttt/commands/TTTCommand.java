package net.mcshockwave.ttt.commands;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.ttt.GameManager;
import net.mcshockwave.ttt.GameWorlds;
import net.mcshockwave.ttt.GameManager.GameState;
import net.mcshockwave.ttt.GameWorlds.GameMap;
import net.mcshockwave.ttt.utils.ParkourManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TTTCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp() && !SQLTable.hasRank(sender.getName(), Rank.JR_MOD)) {
			return false;
		}

		if (args.length < 1) {
			return false;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (args[0].equalsIgnoreCase("world")) {
				World w = Bukkit.getWorld(args[1]);
				p.teleport(w.getSpawnLocation());
			}
			if (args[0].equalsIgnoreCase("listWorlds")) {
				for (World w : Bukkit.getWorlds()) {
					p.sendMessage(w.getName() + ", env " + w.getEnvironment().name());
				}
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
			if (args[0].equalsIgnoreCase("loadWorld")) {
				GameWorlds.addWorld(args[1]);
				p.sendMessage("§aLoaded map " + args[1]);
			}

			if (args[0].equalsIgnoreCase("startcount")) {
				GameManager.startCount();
			}
			if (args[0].equalsIgnoreCase("start")) {
				GameManager.prepare();
			}
			if (args[0].equalsIgnoreCase("stop")) {
				GameManager.stop(null);
			}

			if (args[0].equalsIgnoreCase("lobby")) {
				GameWorlds.generateLobby(GameWorlds.Lobby.w);
			}

			if (args[0].equalsIgnoreCase("exclude")) {
				String n = args[1];
				if (GameManager.exclude.contains(n)) {
					GameManager.exclude.remove(n);
					p.sendMessage("§cRemoved " + n + " from exclusion list");
				} else {
					GameManager.exclude.add(n);
					p.sendMessage("§aAdded " + n + " to exclusion list");
					if (GameManager.state == GameState.GAME && Bukkit.getPlayer(n) != null
							&& GameManager.getPlayers(false).contains(Bukkit.getPlayer(n))) {
						GameManager.spectate(Bukkit.getPlayer(n), false);
					}
				}
			}

			if (args[0].equalsIgnoreCase("parkour")) {
				ParkourManager.generateNew(p, args.length > 1 ? Material.valueOf(args[1].toUpperCase())
						: Material.QUARTZ_BLOCK);
			}
			
			if (args[0].equalsIgnoreCase("fixhealth")) {
				Player t = Bukkit.getPlayer(args[1]);
				t.setMaxHealth(20);
				p.sendMessage("§cFixed health of " + t.getName());
			}
		}

		return true;
	}

}
