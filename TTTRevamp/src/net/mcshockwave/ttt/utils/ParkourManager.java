package net.mcshockwave.ttt.utils;

import net.mcshockwave.ttt.GameWorlds;
import net.mcshockwave.ttt.TroubleInTerroristTown;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class ParkourManager {

	public static Random			rand	= new Random();

	public static int				mult	= 2;

	public static int				spacing	= 50;

	public static ArrayList<Block>	global	= new ArrayList<>();

	public static Location generateNew(Player p, final Material m) {
		World w = GameWorlds.Lobby.w;

		if (!p.getWorld().getName().equals(w.getName())) {
			return null;
		}

		if (mult > 50) {
			mult = 2;
		}

		int x = 100;
		int y = 10;
		int z = mult++ * spacing;

		Location l = new Location(w, x, y, z);

		clearArea(l);

		Block b = l.getBlock();
		b.setType(m);

		ArrayList<Block> blocks = generateJumps(l);

		long delay = 40;
		for (final Block set : blocks) {
			set.setType(m);
			global.add(set);
			new BukkitRunnable() {
				public void run() {
					set.setType(Material.AIR);

					@SuppressWarnings("deprecation")
					FallingBlock fb = set.getWorld().spawnFallingBlock(set.getLocation(), m, (byte) 0);
					fb.setDropItem(false);
					fb.setVelocity(new Vector(0, 0.05, 0));
				}
			}.runTaskLater(TroubleInTerroristTown.ins, delay += 10);
		}

		Location tp = b.getRelative(BlockFace.UP).getLocation();
		tp.setYaw(270);
		p.teleport(tp);

		return l;
	}

	public static void clearArea(Location start) {
		for (int x = start.getBlockX() - 10; x <= start.getBlockX() + 200; x++) {
			for (int y = start.getBlockY() - 10; y <= start.getBlockY() + 10; y++) {
				for (int z = start.getBlockZ() - 20; z <= start.getBlockZ() + 20; z++) {
					if (start.getWorld().getBlockAt(x, y, z).getType() != null) {
						if (!start.getBlock().getChunk().isLoaded()) {
							start.getBlock().getChunk().load();
						}
						start.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
		}
	}

	private static ArrayList<Block> generateJumps(Location start) {
		ArrayList<Block> ret = new ArrayList<>();
		Location l = start.clone();
		// main platform
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				ret.add(l.clone().add(x, 0, z).getBlock());
			}
		}

		l.add(1, 0, 0);

		for (int i = 0; i < 60; i++) {
			l.add(rand.nextInt(4) + 1, rand.nextInt(3) - 1, rand.nextInt(3) - 1);
			if (l.getBlockY() <= start.getBlockY() - 5) {
				l.setY(start.getBlockY() - 5);
			}
			if (l.getBlockY() >= start.getBlockY() + 5) {
				l.setY(start.getBlockY() + 5);
			}
			if (l.getBlockZ() <= start.getBlockZ() - 20) {
				l.setY(start.getBlockZ() - 20);
			}
			if (l.getBlockZ() >= start.getBlockZ() + 20) {
				l.setY(start.getBlockZ() + 20);
			}
			ret.add(l.getBlock());
		}

		// end platform
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				ret.add(l.clone().add(x, 0, z).getBlock());
			}
		}

		return ret;
	}
}
