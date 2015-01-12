package net.mcshockwave.ttt;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.manage.Unpackager;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

	// TODO temp
	public static void generateLobby(World w) {
		Material[] light = { Material.REDSTONE_LAMP_ON, Material.GLOWSTONE };
		Material[] accent = { Material.WOOD, Material.NETHER_BRICK, Material.OBSIDIAN, Material.PACKED_ICE,
				Material.SOUL_SAND };
		Material[] base = { Material.GRASS, Material.QUARTZ_BLOCK, Material.NETHER_BRICK, Material.SNOW_BLOCK,
				Material.NETHERRACK };

		Random rand = new Random();

		Material li = light[rand.nextInt(light.length)];
		int bid = rand.nextInt(accent.length);
		Material ac = accent[bid];
		Material ba = base[bid];

		Material wall = Material.GLASS;

		int rad = 35;
		int yval = 97;
		for (int x = -rad; x <= rad; x++) {
			for (int z = -rad; z <= rad; z++) {
				for (int y = yval; y <= ((Math.abs(x) == rad || Math.abs(z) == rad) ? yval + 6 : yval + 1); y++) {
					// Code is so messy but it works
					Material m = (x % 5 == 0 && z % 5 == 0 && y == yval) ? Material.REDSTONE_BLOCK : (x % 5 == 0
							&& z % 5 == 0 && y == yval + 1) ? li : ((x % 5 == 0 || z % 5 == 0) && y == yval + 1) ? ac
							: y > yval + 1 ? wall : ba;
					Block b = w.getBlockAt(x, y, z);
					if (b.getType() != m) {
						b.setType(m);
					}
				}
			}
		}
	}
}
