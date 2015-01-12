package net.mcshockwave.ttt;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.ttt.GameManager.GameState;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.utils.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DefaultListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if (GameManager.state == GameState.IDLE && GameManager.getPlayers().size() >= GameManager.minPlayers
				&& GameManager.state == GameState.IDLE) {
			MCShockwave.broadcast("%s has reached minimum player count!", "Game");
			GameManager.startCount();
		}

		PlayerUtils.resetPlayer(p);

		p.teleport(getRespawnLocation(p));

		if (GameManager.state == GameState.PREPARING) {
			GameManager.basicKit(p);
			GameManager.registerScoreboard(p);
		} else if (GameManager.state == GameState.GAME) {
			GameManager.spectate(p, true);
			GameManager.registerScoreboard(p);
		}
	}

	public void onQuit(Player p) {
		if (GameManager.state == GameState.GAME) {
			GameManager.onDeath(p, null);
		}

		if (GameManager.getPlayers().size() <= GameManager.minPlayers && GameManager.state != GameState.IDLE) {
			if (GameManager.count != null) {
				GameManager.count.terminate();
			}

			GameManager.stop(null);

			MCShockwave.broadcast("%s cancelled due to lack of players", "Game");
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
	public void onEntityDamage(EntityDamageEvent event) {
		Entity ee = event.getEntity();

		if (ee instanceof Player) {
			Player p = (Player) ee;

			if (ee.getWorld().getName().equals(GameWorlds.Lobby.name())) {
				event.setCancelled(true);
			}

			if (ee.getWorld().getName().equals(GameWorlds.getGameWorld().getName())
					&& GameManager.state == GameState.PREPARING) {
				event.setCancelled(true);
			}

			if (GameManager.specs.contains(p.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			// Player p = (Player) ee;
			Player d = (Player) de;

			if (GameManager.specs.contains(d.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().getType() == Material.IRON_SWORD) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player p = event.getEntity();

		event.setKeepInventory(true);
		event.setKeepLevel(true);

		if (GameManager.state == GameState.GAME) {
			GameManager.spectate(p, false);
		}

		event.setDeathMessage("");

		GameManager.onDeath(p, event);

		PlayerRespawnEvent pre = new PlayerRespawnEvent(p, getRespawnLocation(p), false);
		Bukkit.getPluginManager().callEvent(pre);
		p.setHealth(p.getMaxHealth());
		p.teleport(pre.getRespawnLocation());
		new BukkitRunnable() {
			public void run() {
				p.setVelocity(new Vector());
				p.setFireTicks(0);
			}
		}.runTaskLater(TroubleInTerroristTown.ins, 1);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();

		event.setRespawnLocation(getRespawnLocation(p));
	}

	public static Location getRespawnLocation(Player p) {
		if (GameManager.state == GameState.LOBBY || GameManager.state == GameState.END
				|| GameManager.state == GameState.IDLE) {
			return GameWorlds.Lobby.w.getSpawnLocation();
		}
		if (GameManager.specs.contains(p.getName())) {
			return FileElements.getLoc("lobby", GameWorlds.getGameWorld());
		} else
			return FileElements.getLoc("TTT-spawnpoint", GameWorlds.getGameWorld());
	}

}
