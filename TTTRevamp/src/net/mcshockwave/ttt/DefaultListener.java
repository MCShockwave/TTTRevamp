package net.mcshockwave.ttt;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.ttt.GameManager.GameState;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DefaultListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Player p = event.getPlayer();
		if (GameManager.state == GameState.IDLE && Bukkit.getOnlinePlayers().size() >= GameManager.minPlayers
				&& GameManager.state == GameState.IDLE) {
			MCShockwave.broadcast("Game has reached minimum player count!");
			GameManager.startCount();
		}
	}

	public void onQuit(Player p) {
		if (Bukkit.getOnlinePlayers().size() <= GameManager.minPlayers && GameManager.state != GameState.IDLE) {
			if (GameManager.count != null) {
				GameManager.count.terminate();
			}
			
			GameManager.stop();
			
			MCShockwave.broadcast("Game cancelled due to lack of players.");
			GameManager.state = GameState.IDLE;
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		onQuit(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerQuitEvent event) {
		onQuit(event.getPlayer());
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			if (ee.getWorld().equals(de.getWorld()) && ee.getWorld().getName().equals(GameWorlds.Lobby.name())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getWorld().getName().equals(GameWorlds.Lobby.name())
				&& event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

}
