package net.mcshockwave.ttt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;

public enum GameWorlds {

	Lobby,
	Game;

	public World	w;

	private GameWorlds() {
		generate();
	}

	public void generate() {
		WorldCreator wc = new WorldCreator(name()).generateStructures(false).environment(Environment.NORMAL);
		w = Bukkit.createWorld(wc);
		w.setSpawnLocation(0, 100, 0);

		if (name().equals("Lobby")) {
			generateLobby(w);
		}
	}

	public static void init() {
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
