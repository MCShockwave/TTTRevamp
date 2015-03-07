package net.mcshockwave.ttt.utils;

import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.ttt.TroubleInTerroristTown;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BlockUtils {

	public static HashMap<String, List<BlockState>>	saved	= new HashMap<>();

	public static void save(Location c1, Location c2, String saveTo) {
		save(c1, c2, saveTo, false, 0, false);
	}

	public static void save(Location c1, Location c2, String saveTo, boolean delete, double delay,
			final boolean particles) {
		int x1 = c1.getBlockX();
		int y1 = c1.getBlockY();
		int z1 = c1.getBlockZ();

		int x2 = c2.getBlockX();
		int y2 = c2.getBlockY();
		int z2 = c2.getBlockZ();

		SchedulerUtils ut = SchedulerUtils.getNew();
		double del = 0;
		ArrayList<Block> bs = new ArrayList<>();

		List<BlockState> sa = new ArrayList<>();
		for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
			for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
				for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
					Location loc = new Location(c1.getWorld(), x, y, z);
					final Block b = loc.getBlock();

					if (!b.getChunk().isLoaded()) {
						b.getChunk().load(true);
					}

					sa.add(b.getState());

					if (delete) {
						if (delay > 0) {
							bs.add(b);
						} else {
							b.setType(Material.AIR);
						}
					}
				}
			}
		}
		Collections.shuffle(sa);
		Collections.shuffle(bs);

		if (bs.size() > 0) {
			for (final Block b : bs) {
				Bukkit.getScheduler().runTaskLater(TroubleInTerroristTown.ins, new Runnable() {
					public void run() {
						if (particles) {
							b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
						}
						b.setType(Material.AIR);
					}
				}, (long) (del += delay));
			}
		}

		saved.remove(saveTo);
		saved.put(saveTo, sa);

		ut.execute();
	}

	@SuppressWarnings("deprecation")
	public static void load(Location c1, String savedTo, final double delay, final boolean particles) {
		List<BlockState> states = saved.get(savedTo);
		double del = 0;

		for (final BlockState bs : states) {
			Bukkit.getScheduler().runTaskLater(TroubleInTerroristTown.ins, new Runnable() {
				public void run() {
					Block b = bs.getLocation().getBlock();

					b.setType(bs.getType());
					b.setData(bs.getRawData());

					if (delay > 0 && particles) {
						b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
					}
				}
			}, (long) (del += delay));
		}
	}

	public static void setBlocks(final Location s, Location e, final Material m, final DyeColor color) {
		int x = s.getBlockX();
		int z = s.getBlockZ();
		int x2 = e.getBlockX();
		int z2 = e.getBlockZ();
		int i = 0;
		for (int x3 = Math.min(x, x2); x3 <= Math.max(x, x2); x3++) {
			for (int z3 = Math.min(z, z2); z3 <= Math.max(z, z2); z3++) {
				final int x4 = x3;
				final int z4 = z3;
				Bukkit.getScheduler().runTaskLater(TroubleInTerroristTown.ins, new Runnable() {
					public void run() {
						setBlock(s.getWorld().getBlockAt(x4, s.getBlockY(), z4), m, color);
					}
				}, i);
				i++;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void setBlocks(final Location s, Location e, final Material m, final int color) {
		setBlocks(s, e, m, DyeColor.getByData((byte) color));
	}

	@SuppressWarnings("deprecation")
	public static void setBlock(Block b, Material m, DyeColor color) {
		if (m.getId() == 0) {
			// PacketUtils.sendPacketGlobally(b.getLocation(), 50,
			// PacketUtils.generateBlockParticles(b.getType(), b.getData(),
			// b.getLocation()));
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType().getId());
		} else {
			// PacketUtils.sendPacketGlobally(b.getLocation(), 50,
			// PacketUtils.generateBlockParticles(m, color.getData(),
			// b.getLocation()));
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, m.getId());
		}
		b.setType(m);
		b.setData(color.getData());
	}

}
