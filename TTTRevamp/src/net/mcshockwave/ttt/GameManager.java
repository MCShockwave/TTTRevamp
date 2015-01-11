package net.mcshockwave.ttt;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.ttt.GameWorlds.GameMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

public class GameManager {

	public static final int	minPlayers	= 2;

	public static Random	rand		= new Random();

	public static void enable() {
		GameWorlds.init();
		if (Bukkit.getOnlinePlayers().size() >= minPlayers) {
			startCount();
		}
	}

	public static SchedulerUtils	count	= null;

	public static void startCount() {
		count = SchedulerUtils.getNew();
		int[] broad = { 45, 30, 15, 10, 5, 4, 3, 2, 1 };
		for (int i = 0; i < broad.length; i++) {
			final int time = broad[i];
			count.add(new Runnable() {
				public void run() {
					MCShockwave.broadcast("Game starting in %s second" + (time == 1 ? "" : "s"), time);

					if (time == 15) {
						GameMap gm = GameWorlds.mapList.get(rand.nextInt(GameWorlds.mapList.size()));
						GameWorlds.addWorld(gm.world);

						MCShockwave.broadcast("Map Chosen: %s by %s", gm.name, gm.author);
					}

					if (time <= 5) {
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.playSound(p.getLocation(), Sound.ORB_PICKUP, 10, (float) time / 2.5f);
						}
					}
				}
			});
			int delay = (i >= broad.length - 1 ? 1 : (time - broad[i + 1])) * 20;
			count.add(delay);
		}
		count.add(new Runnable() {
			public void run() {
				start();
			}
		});
		count.execute();
	}

	public static void start() {
		state = GameState.PREPARING;

		MCShockwave.broadcast("Game %s!", "started");
	}
	
	public static void stop() {
		state = GameState.END;
	}

	public static GameState	state	= GameState.IDLE;

	public static enum GameState {
		IDLE,
		LOBBY,
		PREPARING,
		GAME,
		END;
	}

}
