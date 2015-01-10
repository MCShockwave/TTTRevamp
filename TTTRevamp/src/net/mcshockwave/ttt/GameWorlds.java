package net.mcshockwave.ttt;

import net.mcshockwave.ttt.world.BlankGenerator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public enum GameWorlds {

	Lobby,
	Game;

	public World	w;

	private GameWorlds() {
		w = Bukkit.createWorld(new WorldCreator(name()).environment(Environment.NORMAL).type(WorldType.FLAT)
				.generator(new BlankGenerator()));
	}
}
