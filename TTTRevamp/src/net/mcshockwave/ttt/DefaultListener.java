package net.mcshockwave.ttt;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.descriptors.GunType;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.ttt.CorpseManager.Corpse;
import net.mcshockwave.ttt.GameManager.GameState;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.shop.ShopManager;
import net.mcshockwave.ttt.utils.ParkourManager;
import net.mcshockwave.ttt.utils.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang.WordUtils;

public class DefaultListener implements Listener {

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();

		if (KarmaManager.getKarma(p.getName()) <= KarmaManager.KARMA_LIMIT) {
			event.setResult(Result.KICK_BANNED);
			event.setKickMessage("§cYour karma is too low! It will be reset within a day!");
		}
	}

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
		} else if (GameManager.state == GameState.LOBBY || GameManager.state == GameState.IDLE) {
			GameManager.lobbyKit(p);
		}
	}

	public static HashMap<String, Location>	teleporter	= new HashMap<>();

	public static Random					rand		= new Random();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		final Block b = event.getClickedBlock();
		ItemStack it = event.getItem();

		if (GameManager.specs.contains(p.getName())) {
			event.setCancelled(true);
		}

		if (it != null && it.getType() != Material.AIR) {
			if (it.getType() == Material.CLAY_BALL) {
				if (event.getAction().name().contains("LEFT_CLICK")) {
					teleporter.remove(p.getName());
					teleporter.put(p.getName(), p.getLocation());
					MCShockwave.send(ChatColor.GREEN, p, "Location %s", "set");
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

			if (it.getType() == Material.DIAMOND && p.getWorld().getName().equals(GameWorlds.Lobby.w.getName())) {
				if (p.getLocation().getBlockY() > 64 || p.getLocation().getBlockY() < 0) {
					Material[] choose = { Material.GRASS, Material.WOOD, Material.COBBLESTONE, Material.LAPIS_BLOCK,
							Material.WOOL, Material.SNOW_BLOCK, Material.NETHERRACK, Material.SMOOTH_BRICK };
					ParkourManager.generateNew(p, choose[rand.nextInt(choose.length)]);
				}
			}
		}

		if (GameManager.specs.contains(p.getName()) && it != null && it.getType() == Material.FEATHER
				&& event.getAction() != Action.PHYSICAL) {
			int size = GameManager.getPlayers(false).size();
			ItemMenu m = new ItemMenu("Teleport to Players", size);
			int id = 0;
			for (final Player sp : GameManager.getPlayers(false)) {
				Button tp = new Button(true, Material.SKULL_ITEM, 1, 3, sp.getName());
				ItemMetaUtils.setHeadName(tp.button, sp.getName());
				tp.setOnClick(new ButtonRunnable() {
					public void run(Player p, InventoryClickEvent event) {
						p.teleport(sp.getLocation());
					}
				});
				m.addButton(tp, id++);
			}
			m.open(p);
		}

		if (it != null && it.getType() == Material.WRITTEN_BOOK && event.getAction().name().contains("RIGHT_CLICK")) {
			event.setCancelled(false);
		}

		if (it != null && it.getType() == Material.QUARTZ && event.getAction().name().contains("RIGHT_CLICK")) {
			for (Block los : p.getLineOfSight(null, 100)) {
				boolean found = false;
				for (Corpse c : CorpseManager.corpList) {
					if (c.ent.getLocation().distanceSquared(los.getLocation()) < 2 * 2) {
						c.onClick(p);
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}

		if (b != null && b.getType() == Material.SKULL && !GameManager.specs.contains(p.getName())) {
			if (it != null && it.getType() == Material.FLINT) {
				b.setType(Material.AIR);
				p.setItemInHand(null);
				if (c4Tasks.containsKey(b.getLocation())) {
					c4Tasks.get(b.getLocation()).cancel();
					c4Tasks.remove(b.getLocation());
					c4Wire.remove(b.getLocation());
				}
			} else if (c4Tasks.containsKey(b.getLocation())) {
				ItemMenu m = new ItemMenu("C4", 9);

				for (int i = 0; i < c4Colors.length; i++) {
					final int id = i;
					final int idCor = c4Wire.get(b.getLocation());
					Button w = new Button(true, Material.WOOL, 1, c4Colors[i].getWoolData(), c4ChatColors[i]
							+ WordUtils.capitalizeFully(c4Colors[i].name()), "Click to cut wire");
					w.setOnClick(new ButtonRunnable() {
						public void run(Player p, InventoryClickEvent event) {
							if (id == idCor) {
								b.setType(Material.AIR);
								if (c4Tasks.containsKey(b.getLocation())) {
									c4Tasks.get(b.getLocation()).cancel();
									c4Tasks.remove(b.getLocation());
									c4Wire.remove(b.getLocation());
								}
							} else {
								explodeC4(b.getLocation());
							}
						}
					});
					m.addButton(w, i * 2);
				}

				m.open(p);
			}
		}

		if (b != null && event.getBlockFace() != null && it != null && p.getItemInHand().getType() == Material.TNT) {
			p.setItemInHand(null);

			final Block set = b.getRelative(event.getBlockFace());
			set.setType(Material.SKULL);
			Skull s = (Skull) set.getState();
			org.bukkit.material.Skull ms = (org.bukkit.material.Skull) s.getData();
			ms.setFacingDirection(BlockFace.DOWN);
			s.setData(ms);
			s.setRotation(BlockFace.NORTH_WEST);
			s.setSkullType(SkullType.PLAYER);
			s.setOwner("MHF_TNT");
			s.update(true);

			int id = rand.nextInt(c4Colors.length);
			DyeColor color = c4Colors[id];
			ChatColor ccolor = c4ChatColors[id];
			c4Wire.put(set.getLocation(), id);
			planterWire.remove(p.getName());
			planterWire.put(p.getName(), id);

			MCShockwave.send(ccolor, p, "The correct wire is %s", WordUtils.capitalizeFully(color.name()));

			c4Tasks.put(set.getLocation(), new BukkitRunnable() {
				public double	timer		= 45;
				public double	tickNeeded	= getTime();
				public double	tick		= 0;

				public void run() {
					timer -= 0.05;
					tick += 0.05;
					if (tick >= tickNeeded) {
						tick = 0;
						tickNeeded -= 0.05;
						set.getWorld().playSound(set.getLocation(), Sound.CLICK, 0.25f, 1.5f);
						PacketUtils.playBlockParticles(Material.TNT, 0, set.getLocation());
					}

					if (timer <= 0) {
						explodeC4(set.getLocation());
					}
				}

				public double getTime() {
					double startTime = timer;
					double startTickNeeded = 0;
					double totalTime = 0;

					while (totalTime < startTime) {
						startTickNeeded += 0.05;
						totalTime += startTickNeeded;
					}

					return startTickNeeded;
				}
			}.runTaskTimer(TroubleInTerroristTown.ins, 1, 1));
		}
	}

	public static final DyeColor[]				c4Colors		= { DyeColor.RED, DyeColor.GREEN, DyeColor.BLUE,
			DyeColor.YELLOW, DyeColor.WHITE					};
	public static final ChatColor[]				c4ChatColors	= { ChatColor.RED, ChatColor.GREEN, ChatColor.AQUA,
			ChatColor.YELLOW, ChatColor.WHITE					};
	public static HashMap<Location, Integer>	c4Wire			= new HashMap<>();
	public static HashMap<Location, BukkitTask>	c4Tasks			= new HashMap<>();
	public static HashMap<String, Integer>		planterWire		= new HashMap<>();

	@SuppressWarnings("deprecation")
	public static void explodeC4(Location l) {
		l.getWorld().playSound(l, Sound.EXPLODE, 10, 0);
		for (Player p : l.getWorld().getPlayers()) {
			PacketUtils.sendPacket(p, PacketUtils.generateParticles(ParticleEffect.FLAME, l, 0, 2, 1000));

			if (GameManager.specs.contains(p.getName()))
				continue;
			if (p.getLocation().distanceSquared(l) < 15 * 15) {
				p.setLastDamageCause(new EntityDamageEvent(p, DamageCause.BLOCK_EXPLOSION, p.getMaxHealth()));
				p.damage(p.getMaxHealth() * 20);
			} else if (p.getLocation().distanceSquared(l) < 25 * 25) {
				p.setFireTicks((int) ((p.getMaxHealth() * 20) + 30));
			}
		}

		l.getBlock().setType(Material.AIR);
		if (c4Tasks.containsKey(l)) {
			c4Tasks.get(l).cancel();
		}
		c4Tasks.remove(l);
		c4Wire.remove(l);
	}

	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent event) {
		final Projectile e = event.getEntity();
		if (e instanceof Egg) {
			final Item i = e.getWorld().dropItem(e.getLocation(), new ItemStack(Material.EGG));
			i.setPickupDelay(Integer.MAX_VALUE);
			i.setVelocity(new Vector());
			new BukkitRunnable() {
				public int	tick	= 200;

				public void run() {
					tick--;
					PacketUtils.playParticleEffect(ParticleEffect.FLAME, i.getLocation(), 3, 0.05f, 100);
					for (Entity e : i.getNearbyEntities(4, 4, 4)) {
						if (e.getFireTicks() <= 0) {
							e.setFireTicks(40);
						}
					}

					i.remove();

					if (tick <= 0) {
						cancel();
					}
				}
			}.runTaskTimer(TroubleInTerroristTown.ins, 60, 2);
		}
		if (e instanceof Snowball) {
			final Item i = e.getWorld().dropItem(e.getLocation(), new ItemStack(Material.SNOW_BALL));
			i.setPickupDelay(Integer.MAX_VALUE);
			i.setVelocity(new Vector());
			new BukkitRunnable() {
				public int	tick	= 200;

				public void run() {
					tick--;

					PacketUtils.playParticleEffect(ParticleEffect.LARGE_SMOKE, i.getLocation(), 4, 0.05f, 100);
					for (Entity e : i.getNearbyEntities(5, 5, 5)) {
						if (e instanceof LivingEntity) {
							LivingEntity le = (LivingEntity) e;
							le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
						}
					}

					i.remove();

					if (tick <= 0) {
						cancel();
					}
				}
			}.runTaskTimer(TroubleInTerroristTown.ins, 60, 2);
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
		if (event.getEntity() != null && event.getEntityType() == EntityType.PRIMED_TNT
				&& GameManager.state == GameState.GAME) {
			ArrayList<Player> bef = new ArrayList<>();
			ArrayList<Player> aft = new ArrayList<>();
			for (Entity e : event.getEntity().getNearbyEntities(25, 25, 25)) {
				if (e instanceof Player) {
					Player p = (Player) e;
					if (GameManager.specs.contains(p.getName())) {
						continue;
					}
					if (Role.getRole(p) == Role.Traitor || event.getEntity().getVehicle() != null
							&& p.equals(event.getEntity().getVehicle())) {
						aft.add(p);
					} else
						bef.add(p);
				}
			}
			for (Player p : bef) {
				double distSq = p.getLocation().distanceSquared(event.getLocation());
				dmgJihad(p, distSq);
			}
			for (Player p : aft) {
				double distSq = p.getLocation().distanceSquared(event.getLocation());
				dmgJihad(p, distSq);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void dmgJihad(Player p, double distSq) {
		if (distSq < 15 * 15) {
			p.setLastDamageCause(new EntityDamageEvent(p, DamageCause.BLOCK_EXPLOSION, p.getMaxHealth()));
			p.damage(p.getMaxHealth() * 20);
		} else if (distSq < 25 * 25) {
			p.setFireTicks(250);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();

		if (event.getMessage().startsWith("!")) {
			if (Role.getRole(p) == Role.Traitor) {
				for (Player r : Bukkit.getOnlinePlayers()) {
					if (Role.getRole(r) != Role.Traitor) {
						event.getRecipients().remove(r);
					}
				}
				event.setMessage("§c" + event.getMessage());
			} else {
				event.setCancelled(true);
				p.sendMessage("§cYou are not a traitor!");
			}
		}

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

		KarmaManager.changeKarmaFor(p.getName(), false);
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

		if (CorpseManager.getCorpseFromEntity(ee) != null) {
			event.setCancelled(true);
			ee.setFireTicks(0);
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		Entity e = event.getRightClicked();

		if (CorpseManager.getCorpseFromEntity(e) != null) {
			Corpse c = CorpseManager.getCorpseFromEntity(e);
			c.onClick(p);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (CorpseManager.getCorpseFromEntity(event.getEntity()) != null) {
			event.setTarget(null);
			event.setCancelled(true);
		}
	}

	public static HashMap<Entity, Integer>	healerHealth	= new HashMap<>();

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (CorpseManager.getCorpseFromEntity(ee) != null && de instanceof Player) {
			Player d = (Player) de;
			Corpse c = CorpseManager.getCorpseFromEntity(ee);

			if (Gun.fromItem(d.getItemInHand()) != null && Gun.fromItem(d.getItemInHand()) == Gun.TTT_FLARE_GUN) {
				Location l = c.ent.getLocation();
				PacketUtils.playParticleEffect(ParticleEffect.FLAME, l, 0.2f, 0.5f, 50);
				c.ent.remove();
			}
		}

		if (de instanceof Player) {
			Player d = (Player) de;

			if (Gun.fromItem(d.getItemInHand()) != null && Gun.fromItem(d.getItemInHand()) == Gun.TTT_POLTERGEIST) {
				event.setCancelled(true);
				ee.setVelocity(LocUtils.getVelocity(d.getLocation(), ee.getLocation()).multiply(1.5).setY(0.5f));
			}
		}

		if (ee instanceof Player && de instanceof Player) {
			Player p = (Player) ee;
			final Player d = (Player) de;

			if (GameManager.specs.contains(d.getName()) || GameManager.specs.contains(p.getName())) {
				event.setCancelled(true);
				return;
			}

			if (d.getItemInHand() != null && d.getItemInHand().getType() != Material.AIR) {
				ItemStack it = d.getItemInHand();
				if (Gun.fromItem(it) != null && Gun.fromItem(it) == Gun.TTT_GOLDEN_GUN) {
					if (Role.getRole(p) == Role.Traitor) {
						d.sendMessage("§aSuccess! Target was a traitor!");
						event.setDamage(p.getMaxHealth() * 10);
					} else {
						d.sendMessage("§cFailure! Target was innocent!");
						d.damage(d.getMaxHealth() * 10);
					}
				}
				if (Gun.fromItem(it) != null && Gun.fromItem(it) == Gun.TTT_UMP) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
				}
				if (Gun.fromItem(it) != null && Gun.fromItem(it) == Gun.TTT_POISON_GUN) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 0));
				}
				if (Gun.fromItem(it) != null && Gun.fromItem(it) == Gun.TTT_FLARE_GUN) {
					p.setFireTicks((int) (p.getMaxHealth() * 30));
				}
				if (it.getType() == Material.GOLD_SWORD) {
					event.setDamage(p.getMaxHealth() * 10);
					new BukkitRunnable() {
						public void run() {
							d.setItemInHand(null);
						}
					}.runTaskLater(TroubleInTerroristTown.ins, 1);
				}
			} else {
				event.setDamage(0);
			}

			if (GameManager.state == GameState.GAME) {
				Role pr = Role.getRole(p);
				Role dr = Role.getRole(d);

				int dmg = (int) (event.getDamage() > p.getHealth() ? p.getHealth() : event.getDamage());
				if (dr != Role.Traitor) {
					if (pr == Role.Traitor) {
						KarmaManager.addKarma(d.getName(), dmg * 3);
					}
					if (pr == Role.Innocent) {
						KarmaManager.addKarma(d.getName(), dmg * -2);
					}
					if (pr == Role.Detective) {
						KarmaManager.addKarma(d.getName(), dmg * -3);
					}
				} else {
					if (pr == Role.Traitor) {
						KarmaManager.addKarma(d.getName(), dmg * -4);
					}
					if (pr == Role.Innocent) {
						KarmaManager.addKarma(d.getName(), dmg * 2);
					}
					if (pr == Role.Detective) {
						KarmaManager.addKarma(d.getName(), dmg * 3);
					}
				}
			}
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle().getType() == EntityType.BOAT && event.getAttacker().getType() == EntityType.PLAYER) {
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
		Material m = event.getItemDrop().getItemStack().getType();
		Material[] disabled = { Material.IRON_SWORD, Material.PAPER, Material.GOLD_INGOT, Material.WRITTEN_BOOK,
				Material.FEATHER };
		for (Material mc : disabled) {
			if (m == mc)
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

		Location dl = p.getLocation();

		event.setKeepInventory(true);
		event.setKeepLevel(true);

		event.setDeathMessage("");

		boolean isSpec = GameManager.specs.contains(p.getName());

		if (GameManager.state == GameState.GAME) {
			GameManager.onDeath(p, event);
		}
		if (GameManager.state == GameState.GAME && !GameManager.specs.contains(p.getName())) {
			GameManager.spectate(p, false);
		}

		boolean becameSpec = GameManager.specs.contains(p.getName()) && !isSpec;

		PlayerRespawnEvent pre = new PlayerRespawnEvent(p, getRespawnLocation(p), false);
		Bukkit.getPluginManager().callEvent(pre);
		p.setMaxHealth(20);
		p.setHealth(p.getMaxHealth());
		p.teleport(becameSpec ? dl : pre.getRespawnLocation());
		new BukkitRunnable() {
			public void run() {
				p.setVelocity(new Vector());
				p.setFireTicks(0);
				PlayerUtils.clearEffects(p);
				if (GameManager.specs.contains(p.getName())) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
				}
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
	public void onHangingBreak(HangingBreakEvent event) {
		if (event.getCause() != RemoveCause.PHYSICS) {
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
		} else {
			return FileElements.getLoc("TTT-spawnpoint", GameWorlds.getGameWorld());
		}
	}
}
