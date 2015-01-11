package net.mcshockwave.ttt;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.ttt.manage.Unpackager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public enum GameWorlds {

	Lobby,
	Game;

	public World	w;

	private GameWorlds() {
		generate();
	}

	public void generate() {
		WorldCreator wc = new WorldCreator(name()).generateStructures(false).environment(Environment.NORMAL)
				.type(WorldType.FLAT);
		w = Bukkit.createWorld(wc);
		w.setSpawnLocation(0, 100, 0);

		if (name().equals("Lobby")) {
			generateLobby(w);
		}
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

	// TODO temp
	private static void generateLobby(World w) {
		int rad = 35;
		int yval = 97;
		for (int x = -rad; x <= rad; x++) {
			for (int z = -rad; z <= rad; z++) {
				for (int y = yval; y <= ((Math.abs(x) == rad || Math.abs(z) == rad) ? yval + 6 : yval + 1); y++) {
					Material m = (x % 5 == 0 && z % 5 == 0 && y == yval + 1) ? Material.GLOWSTONE : Material.BEDROCK;
					Block b = w.getBlockAt(x, y, z);
					if (b.getType() != m) {
						b.setType(m);
					}
				}
			}
		}
	}
}
