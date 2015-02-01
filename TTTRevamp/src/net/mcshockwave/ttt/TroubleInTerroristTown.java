package net.mcshockwave.ttt;

import net.mcshockwave.MCS.Entities.CustomEntityRegistrar;
import net.mcshockwave.ttt.CorpseManager.CorpseEntity;
import net.mcshockwave.ttt.GameManager.GameState;
import net.mcshockwave.ttt.commands.TTTCommand;
import net.mcshockwave.ttt.utils.ParkourManager;
import net.minecraft.server.v1_7_R4.EntitySkeleton;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
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

		CustomEntityRegistrar.addCustomEntity("CorpseEntity", EntityType.SKELETON, EntitySkeleton.class,
				CorpseEntity.class);

		new BukkitRunnable() {
			public void run() {
				GameWorlds.updateMapList();
			}
		}.runTaskLater(ins, 20);
	}

	@Override
	public void onDisable() {
		for (Block b : ParkourManager.global) {
			b.setType(Material.AIR);
		}
	}

}
