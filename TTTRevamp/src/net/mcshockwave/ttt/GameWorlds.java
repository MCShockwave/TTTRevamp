package net.mcshockwave.ttt;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.manage.Unpackager;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum GameWorlds {

	Lobby;

	public World	w;

	private GameWorlds() {
		w = generate(name());
	}

	public static World generate(String name) {
		WorldCreator wc = new WorldCreator(name).generateStructures(false).environment(Environment.NORMAL)
				.type(WorldType.FLAT);
		World ret = Bukkit.createWorld(wc);
		if (FileElements.has("TTT-spawnpoint", ret)) {
			Location l = FileElements.getLoc("TTT-spawnpoint", ret);
			ret.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		} else
			ret.setSpawnLocation(0, 100, 0);

		if (name.equals("Lobby")) {
			generateLobby(ret);
		}

		return ret;
	}

	public static void init() {
	}

	public static ArrayList<GameMap>	mapList	= new ArrayList<>();

	public static class GameMap {
		public String	name, world, author;

		public GameMap(String name, String world, String author) {
			this.name = name;
			this.world = world;
			this.author = author;
		}
	}

	public static void updateMap(final String map) {
		new BukkitRunnable() {
			public void run() {
				try {
					URL url = new URL("http://mcsw.us/hostserver/Maps/" + map + ".zip");
					File maps = new File("Maps/" + map);
					File del = Unpackager.unpackArchive(url, maps);
					Bukkit.broadcastMessage("§aUpdated map " + map);
					del.delete();
				} catch (Exception ex) {
					ex.printStackTrace();
					Bukkit.broadcastMessage("§cError updating map: " + ex.getLocalizedMessage());
				}
			}
		}.runTaskAsynchronously(TroubleInTerroristTown.ins);
	}

	public static void updateMapList() {
		if (mapList != null) {
			mapList.clear();
		} else
			mapList = new ArrayList<>();
		List<String> maps = SQLTable.TTTMaps.getAll("World");
		for (String s : maps) {
			GameMap gm = new GameMap(SQLTable.TTTMaps.get("World", s, "Name"), s, SQLTable.TTTMaps.get("World", s,
					"Author"));
			mapList.add(gm);
		}
	}

	public static World getGameWorld() {
		for (World w : Bukkit.getWorlds()) {
			boolean isGW = true;
			try {
				valueOf(w.getName());
			} catch (Throwable t) {
				isGW = false;
			}
			if (!isGW && w.getEnvironment() == Environment.NORMAL) {
				return w;
			}
		}
		return null;
	}

	public static void addWorld(final String map) {
		World w = getGameWorld();
		if (w != null) {
			deleteWorld(getGameWorld());
		}

		new BukkitRunnable() {
			public void run() {
				copyWorld("Maps" + File.separator + map, map);
				generate(map);
			}
		}.runTaskLater(TroubleInTerroristTown.ins, 40);
	}

	public static void deleteWorld(final String w) {
		if (Bukkit.getWorld(w) != null) {
			World wld = Bukkit.getWorld(w);
			for (Entity e : wld.getEntities()) {
				if (e instanceof Player) {
					e.teleport(Lobby.w.getSpawnLocation());
				} else {
					e.remove();
				}
			}
			for (Chunk c : wld.getLoadedChunks()) {
				c.unload(false, false);
			}
			if (Bukkit.unloadWorld(w, false)) {
				System.out.println("Unloaded world");
			} else {
				System.err.println("UNLOADING WORLD FAILED");
			}
		}
		Bukkit.getScheduler().runTaskLater(TroubleInTerroristTown.ins, new Runnable() {
			public void run() {
				if (delete(new File(w))) {
					System.out.println("Deleted world!");
				} else {
					System.err.println("DELETING WORLD FAILED");
				}
			}
		}, 20l);
	}

	public static void deleteWorld(World w) {
		deleteWorld(w.getName());
	}

	public static boolean delete(File file) {
		if (file.isDirectory())
			for (File subfile : file.listFiles())
				if (!delete(subfile))
					return false;
		if (!file.delete())
			return false;
		return true;
	}

	public static void copyWorld(String s, String t) {
		File source = new File(s);
		File target = new File(t);
		try {
			copyTo(source, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyTo(File src, File dest) throws IOException {
		if (src.isDirectory()) {

			if (!dest.exists()) {
				dest.mkdir();
			}

			String files[] = src.list();

			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyTo(srcFile, destFile);
			}

		} else {
			if (src.getName().equalsIgnoreCase("uid.dat")) {
				return;
			}

			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}

	public static BukkitTask	lobbyAnimation	= null;
	public static Random		rand			= new Random();

	// TODO temp?
	public static void generateLobby(final World w) {
		if (rand == null) {
			rand = new Random();
		}

		Material[] light = { Material.REDSTONE_LAMP_ON, Material.GLOWSTONE };
		Material[] accent = { Material.WOOD, Material.NETHER_BRICK, Material.OBSIDIAN, Material.PACKED_ICE,
				Material.SMOOTH_BRICK };
		Material[] base = { Material.GRASS, Material.QUARTZ_BLOCK, Material.NETHER_BRICK, Material.SNOW_BLOCK,
				Material.STONE };

		final Material li = light[rand.nextInt(light.length)];
		int bid = rand.nextInt(accent.length);
		final Material ac = accent[bid];
		final Material ba = base[bid];

		final int spacing = 5;

		Material wall = Material.GLASS;

		final int rad = 35;
		final int yval = 97;
		for (int x = -rad; x <= rad; x++) {
			for (int z = -rad; z <= rad; z++) {
				for (int y = yval; y <= ((Math.abs(x) == rad || Math.abs(z) == rad) ? yval + 6 : yval + 1); y++) {
					// Code is so messy but it works
					Material m = (x % spacing == 0 && z % spacing == 0 && y == yval) ? Material.REDSTONE_BLOCK : (x
							% spacing == 0
							&& z % spacing == 0 && y == yval + 1) ? li
							: ((x % spacing == 0 || z % spacing == 0) && y == yval + 1) ? ac : y > yval + 1 ? wall : ba;
					Block b = w.getBlockAt(x, y, z);
					if (b.getType() != m) {
						b.setType(m);
					}
				}
			}
		}

		Block psign = w.getBlockAt(0, yval + 3, rad - 1);
		psign.setType(Material.WALL_SIGN);
		org.bukkit.material.Sign data = (org.bukkit.material.Sign) psign.getState().getData();
		data.setFacingDirection(BlockFace.NORTH);

		Sign s = (Sign) psign.getState();
		s.setLine(0, "[Parkour]");
		s.setLine(2, "Click to go");
		s.setLine(3, "to parkour");
		s.update();

		if (lobbyAnimation != null) {
			lobbyAnimation.cancel();
		}
		lobbyAnimation = new BukkitRunnable() {
			// why did I do this? idk
			public void run() {
				if (w.getPlayers().size() == 0) {
					return;
				}

				// if (rand.nextInt(500) == 0) {
				// int y = yval + 1;
				// for (int x = -rad; x <= rad; x += spacing) {
				// for (int z = -rad; z <= rad; z += spacing) {
				// Block b = w.getBlockAt(x, y, z);
				// if (b.getType() == li) {
				// spawnJumpingBlock(b, rand.nextDouble());
				// }
				// }
				// }
				// }

				if (rand.nextInt(50) == 0) {
					for (int i = 0; i < rand.nextInt(5) + 5; i++) {
						int xco = ((int) (rand.nextInt(rad * 2) - rad) / spacing) * spacing;
						int zco = ((int) (rand.nextInt(rad * 2) - rad) / spacing) * spacing;
						int y = yval + 1;

						double vel = rand.nextDouble() + 0.5;

						for (int x = 0; x <= spacing; x++) {
							for (int z = 0; z <= spacing; z++) {
								Block b = w.getBlockAt(xco + x, y, zco + z);
								if (b.getType() == ba) {
									spawnJumpingBlock(b, vel + (rand.nextGaussian() / 100));
								}
							}
						}
					}
				}
				//
				// if (rand.nextInt(750) == 0) {
				// int xadd = rand.nextBoolean() ? 1 : -1;
				// int zadd = rand.nextBoolean() ? 1 : -1;
				//
				// int y = yval + 1;
				//
				// int xstart = rad * -xadd;
				// int zstart = rad * -zadd;
				//
				// w.playSound(new Location(w, xstart, y, zstart),
				// Sound.FIREWORK_LAUNCH, 1000, 0);
				//
				// final double vel = rand.nextDouble() / 5 + 0.2;
				//
				// double del = 0;
				// for (int d = 0; d <= rad * 2; d++) {
				// del += 1.5;
				// final ArrayList<Block> change = new ArrayList<>();
				//
				// int dx = d * xadd;
				// int dz = d * zadd;
				//
				// int xc = xstart + dx;
				// int zc = zstart + dz;
				//
				// for (int x = xstart; (xadd < 0 ? x >= xc : x <= xc); x +=
				// xadd) {
				// Block b = w.getBlockAt(x, y, zc);
				// if (!change.contains(b)) {
				// change.add(b);
				// }
				// }
				// for (int z = zstart; (zadd < 0 ? z >= zc : z <= zc); z +=
				// zadd) {
				// Block b = w.getBlockAt(xc, y, z);
				// if (!change.contains(b)) {
				// change.add(b);
				// }
				// }
				//
				// new BukkitRunnable() {
				// public void run() {
				// for (Block b : change) {
				// if (b.getType() == ac &&
				// b.getRelative(BlockFace.UP).getType() == Material.AIR) {
				// spawnJumpingBlock(b, vel);
				// }
				// }
				// }
				// }.runTaskLater(TroubleInTerroristTown.ins, (long) del);
				// }
				// }
			}
		}.runTaskTimer(TroubleInTerroristTown.ins, 2, 2);
	}

	public static void spawnJumpingBlock(Block b, double vel) {
		@SuppressWarnings("deprecation")
		FallingBlock fb = b.getWorld()
				.spawnFallingBlock(b.getLocation().clone().add(0, 1, 0), b.getType(), b.getData());
		fb.setVelocity(new Vector(0, vel + (rand.nextGaussian() / 100), 0));
		PacketUtils.playBlockParticles(b.getType(), 0, b.getLocation());

		fb.getWorld().playSound(fb.getLocation(), Sound.CHICKEN_EGG_POP, 4, 0);
		fb.setDropItem(false);

		for (Entity e : fb.getNearbyEntities(3, 3, 3)) {
			if (e instanceof Player) {
				e.setVelocity(fb.getVelocity());
			}
		}
	}
}
