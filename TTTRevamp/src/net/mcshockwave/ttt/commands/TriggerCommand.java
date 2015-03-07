package net.mcshockwave.ttt.commands;

import net.mcshockwave.ttt.TroubleInTerroristTown;
import net.mcshockwave.ttt.utils.BlockUtils;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TriggerCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof BlockCommandSender) {
			final BlockCommandSender bs = (BlockCommandSender) sender;
			if (args.length == 0) {
				bs.sendMessage("§cNeed arguments");
			} else {
				String cmd = args[0];
				if (cmd.equalsIgnoreCase("rem")) {
					if (args.length > 8) {
						final int x1 = Integer.parseInt(args[1]);
						final int y1 = Integer.parseInt(args[2]);
						final int z1 = Integer.parseInt(args[3]);
						int x2 = Integer.parseInt(args[4]);
						int y2 = Integer.parseInt(args[5]);
						int z2 = Integer.parseInt(args[6]);
						int time = Integer.parseInt(args[7]);

						final String save = UUID.randomUUID().toString();
						BlockUtils.save(new Location(bs.getBlock().getWorld(), x1, y1, z1), new Location(bs.getBlock()
								.getWorld(), x2, y2, z2), save, true, 0.1, true);
						new BukkitRunnable() {
							public void run() {
								BlockUtils.load(new Location(bs.getBlock().getWorld(), x1, y1, z1), save, 0.1, true);
							}
						}.runTaskLater(TroubleInTerroristTown.ins, time);
					} else {
						bs.sendMessage("§cUsage: " + label + " rem x1 y1 z1 x2 y2 z2 TIME(ticks)");
					}
				}
			}
		}
		return false;
	}
}
