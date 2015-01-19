package net.mcshockwave.ttt;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.descriptors.GunType;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.ttt.GameManager.GameState;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.shop.ShopManager;
import net.mcshockwave.ttt.utils.ParkourManager;
import net.mcshockwave.ttt.utils.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;

public class DefaultListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if (GameManager.state == GameState.IDLE && GameManager.getPlayers().size() >= GameManager.minPlayers) {
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
			GameManager.updatePlayerLists();
		}
	}

	public static HashMap<String, Location>	teleporter	= new HashMap<>();

	public static Random					rand		= new Random();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		ItemStack it = event.getItem();

		if (b != null) {
			if (b.getType().name().contains("SIGN")) {
				Sign s = (Sign) b.getState();

				if (s.getLine(0).contains("Parkour")) {
					Material[] choose = { Material.GRASS, Material.WOOD, Material.COBBLESTONE, Material.LAPIS_BLOCK,
							Material.WOOL, Material.SNOW_BLOCK, Material.NETHERRACK, Material.SMOOTH_BRICK };
					ParkourManager.generateNew(p, choose[rand.nextInt(choose.length)]);
				}
			}
		}

		if (it != null && it.getType() != Material.AIR) {
			if (it.getType() == Material.CLAY_BALL) {
				if (event.getAction().name().contains("LEFT_CLICK")) {
					teleporter.remove(p.getName());
					teleporter.put(p.getName(), p.getLocation());
				} else if (event.getAction().name().contains("RIGHT_CLICK")) {
					MCShockwave.send(ChatColor.GREEN, p, "%s", "Teleporting...");
					new BukkitRunnable() {
						public void run() {
							p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
							p.teleport(teleporter.get(p.getName()));
							p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
						}
					}.runTaskLater(TroubleInTerroristTown.ins, 60);
				}
			}

			if (it.getType() == Material.SULPHUR) {
				p.setItemInHand(null);

				TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getLocation(), EntityType.PRIMED_TNT);
				p.setPassenger(tnt);

				p.getWorld().playSound(p.getLocation(), Sound.FUSE, 10, 0);
			}
		}
	}

	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent event) {
		final Projectile e = event.getEntity();
		if (e instanceof Egg) {
			final Item i = e.getWorld().dropItem(e.getLocation(), new ItemStack(Material.EGG));
			i.setPickupDelay(Integer.MAX_VALUE);
			i.setVelocity(new Vector());
			new BukkitRunnable() {
				public void run() {
					i.getWorld().createExplosion(i.getLocation(), 0);
					PacketUtils.playParticleEffect(ParticleEffect.FLAME, i.getLocation(), 2, 0.05f, 100);
					for (Entity e : i.getNearbyEntities(8, 8, 8)) {
						e.setFireTicks((int) (200 - e.getLocation().distanceSquared(i.getLocation())));
					}

					i.remove();
				}
			}.runTaskLater(TroubleInTerroristTown.ins, 60);
		}
		if (e instanceof Snowball) {
			final Item i = e.getWorld().dropItem(e.getLocation(), new ItemStack(Material.SNOW_BALL));
			i.setPickupDelay(Integer.MAX_VALUE);
			i.setVelocity(new Vector());
			new BukkitRunnable() {
				public void run() {
					i.getWorld().createExplosion(i.getLocation(), 0);
					PacketUtils.playParticleEffect(ParticleEffect.LARGE_SMOKE, i.getLocation(), 2, 0.05f, 100);
					for (Entity e : i.getNearbyEntities(10, 10, 10)) {
						if (e instanceof LivingEntity) {
							LivingEntity le = (LivingEntity) e;
							le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (200 - e
									.getLocation().distanceSquared(i.getLocation())), 0));
						}
					}

					i.remove();
				}
			}.runTaskLater(TroubleInTerroristTown.ins, 60);
		}
		if (e instanceof EnderPearl) {
			final Item i = e.getWorld().dropItem(e.getLocation(), new ItemStack(Material.ENDER_PEARL));
			i.setPickupDelay(Integer.MAX_VALUE);
			i.setVelocity(new Vector());
			new BukkitRunnable() {
				public void run() {
					i.getWorld().createExplosion(i.getLocation(), 0);
					for (int x = 0; x < 10; x++)
						i.getWorld().playEffect(LocUtils.addRand(i.getLocation(), 4, 4, 4), Effect.ENDER_SIGNAL, 0);
					for (Entity e : i.getNearbyEntities(5, 5, 5)) {
						e.setVelocity(LocUtils.getVelocity(i.getLocation(), e.getLocation()).multiply(0.5));
					}

					i.remove();
				}
			}.runTaskLater(TroubleInTerroristTown.ins, 60);
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause() == TeleportCause.ENDER_PEARL) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().clear();
		if (event.getEntityType() == EntityType.PRIMED_TNT) {
			for (Entity e : event.getEntity().getNearbyEntities(25, 25, 25)) {
				if (e instanceof Player) {
					Player p = (Player) e;

					double distSq = p.getLocation().distance(event.getLocation());
					double dmg = (distSq > 15 * 15) ? p.getMaxHealth() / distSq - (15 * 15) : p.getMaxHealth();
					if (p.hasLineOfSight(event.getEntity())) {
						p.damage(dmg, event.getEntity());
					} else {
						p.damage(dmg / 2, event.getEntity());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		if (GameManager.specs.contains(p.getName()) && !GameManager.exclude.contains(p.getName())
				&& !event.getMessage().startsWith("@")) {
			for (Player r : GameManager.getPlayers(false)) {
				event.getRecipients().remove(r);
			}
			event.setMessage("§7" + event.getMessage());
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		ItemStack cu = event.getCurrentItem();

		if (cu != null && cu.getType() == Material.GOLD_INGOT && cu.hasItemMeta() && ItemMetaUtils.hasCustomName(cu)
				&& ChatColor.stripColor(ItemMetaUtils.getItemName(cu)).equalsIgnoreCase("Open Shop")) {
			event.setCancelled(true);
			p.closeInventory();
			if (Role.getRole(p) != null && Role.getRole(p) != Role.Innocent) {
				ItemMenu m = ShopManager.getMenu(p, Role.getRole(p) == Role.Traitor);
				m.open(p);
			}
		}
		if (cu != null && cu.getType() == Material.PAPER && cu.hasItemMeta() && ItemMetaUtils.hasCustomName(cu)
				&& ChatColor.stripColor(ItemMetaUtils.getItemName(cu)).equalsIgnoreCase("Player List")) {
			event.setCancelled(true);
		}
	}

	public void onQuit(Player p) {
		if (GameManager.state == GameState.GAME) {
			GameManager.onDeath(p, null);
		}

		if (GameManager.getPlayers().size() <= GameManager.minPlayers && GameManager.state != GameState.IDLE
				&& (GameManager.state != GameState.GAME || GameManager.getPlayers().size() <= 1)) {
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

			if (event.getCause() == DamageCause.VOID) {
				p.teleport(p.getWorld().getSpawnLocation());
			}

			if (ee.getWorld().getName().equals(GameWorlds.Lobby.name())) {
				event.setCancelled(true);
			}

			if (GameWorlds.getGameWorld() != null) {
				if (ee.getWorld().getName().equals(GameWorlds.getGameWorld().getName())
						&& GameManager.state == GameState.PREPARING) {
					event.setCancelled(true);
				}
			}

			if (GameManager.specs.contains(p.getName())) {
				event.setCancelled(true);
			}
		}
	}

	public static HashMap<Entity, Integer>	healerHealth	= new HashMap<>();

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			Player p = (Player) ee;
			Player d = (Player) de;

			if (GameManager.specs.contains(d.getName())) {
				event.setCancelled(true);
			}

			if (d.getItemInHand() != null && d.getItemInHand().getType() != Material.AIR) {
				ItemStack it = d.getItemInHand();
				if (it.getType() == Material.GOLD_SWORD) {
					d.setItemInHand(null);
					event.setDamage(p.getMaxHealth() * 10);
				}
			} else {
				event.setDamage(0);
			}
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle().getType() == EntityType.BOAT && healerHealth.containsKey(event.getVehicle())
				&& event.getAttacker().getType() == EntityType.PLAYER) {
			Vehicle ee = event.getVehicle();
			Damageable d = (Damageable) event.getAttacker();

			if (!healerHealth.containsKey(ee)) {
				healerHealth.put(ee, 200);
			}

			if (d.getHealth() < d.getMaxHealth()) {
				int hp = healerHealth.get(ee);
				healerHealth.remove(ee);
				healerHealth.put(ee, --hp);
				if (hp < 0) {
					ee.remove();
				}

				double healthSet = d.getHealth() + 1;
				if (healthSet > d.getMaxHealth()) {
					healthSet = d.getMaxHealth();
				}
				d.setHealth(healthSet);
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if (event.getVehicle().getType() == EntityType.BOAT) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getEntered().getType() == EntityType.PLAYER && event.getVehicle().getType() == EntityType.BOAT) {
			event.setCancelled(true);
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
		if (event.getItemDrop().getItemStack().getType() == Material.IRON_SWORD
				|| event.getItemDrop().getItemStack().getType() == Material.PAPER) {
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
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
		event.setHatching(false);
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		ItemStack it = event.getItem().getItemStack();

		if (GameManager.specs.contains(p.getName())) {
			event.setCancelled(true);
		}

		if (p.getItemOnCursor() != null && p.getItemOnCursor().getType() != Material.AIR) {
			event.setCancelled(true);
			return;
		}

		if (Gun.fromItem(it) != null) {
			Gun g = Gun.fromItem(it);
			for (ItemStack con : p.getInventory().getContents()) {
				if (con != null && con.getType() != Material.AIR && Gun.fromItem(con) != null) {
					Gun cg = Gun.fromItem(con);
					if (g.type == GunType.PISTOL && cg.type == GunType.PISTOL) {
						event.setCancelled(true);
					} else if (g.type != GunType.PISTOL && cg.type != GunType.PISTOL) {
						event.setCancelled(true);
					}
				}
			}
		}

		if (!event.isCancelled() && event.getItem().getLocation().getBlock().getType().name().contains("_PLATE")) {
			event.getItem().getLocation().getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if (event.getEntity().getLocation().getBlock().getType().name().contains("_PLATE")) {
			event.setCancelled(true);
		}
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
