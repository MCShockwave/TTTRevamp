package net.mcshockwave.ttt;

import net.mcshockwave.ttt.commands.TTTCommand;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TroubleInTerroristTown extends JavaPlugin {
	
	public static TroubleInTerroristTown ins;
	
	@Override
	public void onEnable() {
		ins = this;
		
		Bukkit.getPluginManager().registerEvents(new DefaultListener(), ins);
		
		GameManager.enable();
		
		getCommand("ttt").setExecutor(new TTTCommand());
	}

}