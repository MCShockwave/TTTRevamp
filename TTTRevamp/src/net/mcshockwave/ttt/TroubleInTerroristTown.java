package net.mcshockwave.ttt;

import net.mcshockwave.ttt.GameManager.GameState;
import net.mcshockwave.ttt.commands.TTTCommand;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TroubleInTerroristTown extends JavaPlugin {

	public static TroubleInTerroristTown	ins;

	@Override
	public void onEnable() {
		ins = this;

		Bukkit.getPluginManager().registerEvents(new DefaultListener(), ins);

		GameManager.enable();

		getCommand("ttt").setExecutor(new TTTCommand());

		if (Bukkit.getOnlinePlayers().size() >= GameManager.minPlayers) {
			GameManager.state = GameState.LOBBY;
		}

		new BukkitRunnable() {
			public void run() {
				GameWorlds.updateMapList();
			}
		}.runTaskLater(ins, 20);
	}

}
