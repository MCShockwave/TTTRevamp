package net.mcshockwave.ttt;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.descriptors.AmmoType;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.ttt.CorpseManager.Corpse;
import net.mcshockwave.ttt.GameWorlds.GameMap;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.shop.DetectiveShop;
import net.mcshockwave.ttt.shop.ShopManager;
import net.mcshockwave.ttt.shop.TraitorShop;
import net.mcshockwave.ttt.utils.DamageCauseInfo;
import net.mcshockwave.ttt.utils.InfoBook;
import net.mcshockwave.ttt.utils.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang.WordUtils;

public class GameManager {

	public static final int			minPlayers	= 4;

	public static Random			rand		= new Random();

	public static ArrayList<String>	exclude		= new ArrayList<>();

	public static void enable() {
		GameWorlds.init();

		new BukkitRunnable() {
			public void run() {
				if (getPlayers().size() >= minPlayers) {
					startCount();
				}
			}
		}.runTaskLater(TroubleInTerroristTown.ins, 60);
	}

	public static SchedulerUtils	count		= null;
	public static BukkitTask		roundTimer	= null;

	public static GameMap			map			= null;

	public static void startCount() {
		state = GameState.LOBBY;

		for (Player p : getPlayers()) {
			lobbyKit(p);
		}

		count = SchedulerUtils.getNew();
		int[] broad = { 45, 30, 15, 10, 5, 4, 3, 2, 1 };
		for (int i = 0; i < broad.length; i++) {
			final int time = broad[i];
			count.add(new Runnable() {
				public void run() {
					MCShockwave.broadcast("Game starting in %s second" + (time == 1 ? "" : "s"), time);

					if (time == 15) {
						map = GameWorlds.mapList.get(rand.nextInt(GameWorlds.mapList.size()));
						GameWorlds.addWorld(map.world);

						MCShockwave.broadcast("Map Chosen: %s by %s", map.name, map.author);
					}

					if (time == 10) {
						World w = GameWorlds.getGameWorld();
						String[] gr = { "doMobSpawning:false", "doDaylightCycle:false", "doMobLoot:false",
								"doTileDrops:false", "mobGriefing:false" };
						for (String s : gr) {
							String[] ss = s.split(":");
							w.setGameRuleValue(ss[0], ss[1]);
						}

						w.setTime(FileElements.has("time", w.getName()) ? Integer.parseInt(FileElements.get("time",
								w.getName())) : 5000);
					}

					if (time <= 5) {
						for (Player p : getPlayers()) {
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
				prepare();
			}
		});
		count.execute();
	}

	public static int	time	= 0;

	public static void registerScoreboard(Player p) {
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		Scoreboard s = p.getScoreboard();
		s.registerNewObjective("Sidebar", "dummy").setDisplaySlot(DisplaySlot.SIDEBAR);

		if (state == GameState.GAME) {
			Team det = s.registerNewTeam("Detectives");
			det.setAllowFriendlyFire(true);
			det.setCanSeeFriendlyInvisibles(false);
			det.setDisplayName("§9Detectives");
			det.setPrefix("§b§l[D] §9");
			for (Player d : Role.Detective.getPlayers()) {
				det.addEntry(d.getName());
			}

			if (Role.getRole(p) == Role.Traitor) {
				Team tr = s.registerNewTeam("Traitors");
				tr.setAllowFriendlyFire(true);
				tr.setCanSeeFriendlyInvisibles(true);
				tr.setDisplayName("§cTraitors");
				tr.setPrefix("§4§l[T] §c");

				for (Player t : Role.Traitor.getPlayers()) {
					tr.addEntry(t.getName());
				}
			}
		}
		updateScoreboard(p);
	}

	public static void updateScoreboard(Player p) {
		Scoreboard s = p.getScoreboard();
		Objective side = s.getObjective(DisplaySlot.SIDEBAR);

		if (side == null) {
			registerScoreboard(p);
			return;
		}

		String t = String.format("%d:%02d", time / 60, (time % 60));
		side.setDisplayName("§cTTT§7 " + t);

		Score blank = side.getScore("§f");
		blank.setScore(2);

		String prep = "§7Preparing";
		String role = Role.getRole(p) != null ? Role.getRole(p).color + Role.getRole(p).name() : null;

		if (state == GameState.PREPARING) {
			side.getScore(prep).setScore(1);
		}
		if (state == GameState.GAME) {
			if (s.getEntries().contains(prep))
				s.resetScores(prep);

			if (role != null) {
				side.getScore(role).setScore(1);
			}
			if (specs.contains(p.getName())) {
				side.getScore("§8Spectator").setScore(1);
			}
		}
	}

	public static void registerScoreboards() {
		for (Player p : getPlayers()) {
			try {
				registerScoreboard(p);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void updateScoreboards() {
		for (Player p : getPlayers()) {
			try {
				updateScoreboard(p);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void basicKit(Player p) {
		PlayerUtils.clearInv(p);
		p.getInventory().setItem(0, ItemMetaUtils.setItemName(new ItemStack(Material.IRON_SWORD), "§fCrowbar"));
		p.getInventory().setItem(7, AmmoType.TTT_PRIMARY.getItem(20));
		p.getInventory().setItem(8, AmmoType.TTT_SECONDARY.getItem(10));
	}

	public static final Gun[]		validSpawns		= { Gun.TTT_AK47, Gun.TTT_DEAGLE, Gun.TTT_GLOCK, Gun.TTT_HUGE,
			Gun.TTT_M16, Gun.TTT_MAC10, Gun.TTT_RIFLE, Gun.TTT_SHOTGUN };
	public static final AmmoType[]	validAmmo		= { AmmoType.TTT_PRIMARY, AmmoType.TTT_PRIMARY,
			AmmoType.TTT_SECONDARY					};
	public static final ItemStack[]	validGrenades	= {
			ItemMetaUtils.setItemName(new ItemStack(Material.EGG), "§fIncendiary Grenade"),
			ItemMetaUtils.setItemName(new ItemStack(Material.SNOW_BALL), "§fSmoke Grenade"),
			ItemMetaUtils.setItemName(new ItemStack(Material.ENDER_PEARL), "§fDiscombobulator") };

	public static void prepare() {
		state = GameState.PREPARING;

		MCShockwave.broadcast("Game %s!", "started");
		MCShockwave.broadcast(ChatColor.GRAY, "%s", "Preparing...");
		ArrayList<Location> spawns = FileElements.getAll("player-spawn", GameWorlds.getGameWorld());

		for (Player p : getPlayers()) {
			PlayerUtils.resetPlayer(p);
			p.teleport(spawns.get(rand.nextInt(spawns.size())).clone().add(0, 1, 0));
			registerScoreboards();
			basicKit(p);
		}

		ArrayList<Location> guns = FileElements.getAll("gun-spawn", GameWorlds.getGameWorld());
		for (final Location l : guns) {
			l.add(0.5, 1.5, 0.5);
			ItemStack it;
			int r = rand.nextInt(5);
			if (r == 0 || r == 1) {
				l.getBlock().setType(Material.STONE_PLATE);
				it = validSpawns[rand.nextInt(validSpawns.length)].getItem();
			} else if (r == 2 || r == 3) {
				l.getBlock().setType(Material.WOOD_PLATE);
				it = validAmmo[rand.nextInt(validAmmo.length)].getItem(rand.nextInt(16) + 8);
			} else {
				l.getBlock().setType(Material.IRON_PLATE);
				it = validGrenades[rand.nextInt(validGrenades.length)].clone();
			}
			final Item i = l.getWorld().dropItem(l, it);
			i.setVelocity(new Vector());
			new BukkitRunnable() {
				public void run() {
					i.teleport(l);
				}
			}.runTaskLater(TroubleInTerroristTown.ins, 10);
		}

		count = SchedulerUtils.getNew();
		for (int t = 30; t >= 0; t--) {
			final int t2 = t;
			count.add(20);
			count.add(new Runnable() {
				public void run() {
					time = t2;
					updateScoreboards();
				}
			});
		}
		count.add(new Runnable() {
			public void run() {
				begin();
			}
		});
		count.execute();
	}

	public static void begin() {
		state = GameState.GAME;

		time = 0;

		Role.generate();

		updatePlayerLists();

		for (Role r : Role.values()) {
			for (Player p : r.getPlayers()) {
				MCShockwave.send(r.color, p, "You are a" + (isVowel(r.name().charAt(0)) ? "n" : "") + " %s!", r.name());
				PacketUtils.playTitle(p, 10, 40, 10, "§7You are a" + (isVowel(r.name().charAt(0)) ? "n" : "") + " "
						+ r.color + r.name(), "§f");

				if (r != Role.Innocent) {
					p.getInventory().setItem(17,
							ItemMetaUtils.setItemName(new ItemStack(Material.GOLD_INGOT), "§6§lOpen Shop"));

					ShopManager.setCredits(p.getName(), 2);
				}
			}
		}

		int tsi = Role.Traitor.players.size();
		MCShockwave.broadcast(ChatColor.RED, "There " + (tsi == 1 ? "is" : "are") + " %s traitor"
				+ (tsi == 1 ? "" : "s") + " this round", tsi);

		registerScoreboards();

		roundTimer = new BukkitRunnable() {
			public void run() {
				time++;
				updateScoreboards();
			}
		}.runTaskTimer(TroubleInTerroristTown.ins, 20, 20);
	}

	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> ret = new ArrayList<>();

		ret.addAll(Bukkit.getOnlinePlayers());
		for (String s : exclude) {
			if (Bukkit.getPlayer(s) != null) {
				ret.remove(Bukkit.getPlayer(s));
			}
		}

		return ret;
	}

	public static ArrayList<Player> getPlayers(boolean includeSpecs) {
		if (includeSpecs) {
			return getPlayers();
		}
		ArrayList<Player> ret = new ArrayList<>();

		for (Player p : getPlayers()) {
			if (!specs.contains(p.getName())) {
				ret.add(p);
			}
		}

		return ret;
	}

	public static void stop(final String winner) {
		state = GameState.END;

		specs.clear();
		innoSlay.clear();

		if (roundTimer != null) {
			roundTimer.cancel();
		}

		CorpseManager.corpses.clear();
		CorpseManager.corpList.clear();

		MCShockwave.broadcast("Game %s!", "ended");
		if (Role.Traitor.all.size() > 0) {
			int si = Role.Traitor.all.size();
			String ts = "";
			for (int i = 0; i < si; i++) {
				ts += "%s, ";
			}
			ts = ts.substring(0, ts.length() - 2);
			MCShockwave.broadcast(ChatColor.RED, "The traitor" + (si == 1 ? " was" : "s were") + " " + ts,
					Role.Traitor.all.toArray(new Object[0]));
		}

		DefaultListener.healerHealth.clear();
		DefaultListener.teleporter.clear();

		ShopManager.credits.clear();

		for (TraitorShop ts : TraitorShop.values()) {
			ts.timesBought.clear();
		}
		for (DetectiveShop ds : DetectiveShop.values()) {
			ds.timesBought.clear();
		}

		GameWorlds.generateLobby(GameWorlds.Lobby.w);

		count = SchedulerUtils.getNew();
		count.add(60);
		count.add(new Runnable() {
			public void run() {
				for (Player p : getPlayers()) {
					p.teleport(GameWorlds.Lobby.w.getSpawnLocation());
					p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
					PlayerUtils.resetPlayer(p);

					for (Player p2 : getPlayers()) {
						p2.showPlayer(p);
					}
				}

				for (Role r : Role.values()) {
					r.players.clear();
					r.all.clear();
				}
				if (state != GameState.IDLE) {
					state = GameState.LOBBY;
				}
			}
		});
		count.add(20);
		count.add(new Runnable() {
			public void run() {
				if (winner != null) {
					if (getPlayers().size() >= minPlayers) {
						startCount();
					} else {
						MCShockwave.broadcast("Not enough %s!", "players");
						state = GameState.IDLE;
					}
				}
			}
		});
		count.execute();
	}

	public static ArrayList<String>	specs	= new ArrayList<>();

	public static void spectate(Player p, boolean hide) {
		if (!specs.contains(p.getName())) {
			specs.add(p.getName());
		}

		PlayerUtils.resetPlayer(p);
		p.getInventory()
				.setItem(8, ItemMetaUtils.setItemName(new ItemStack(Material.FEATHER), "§6Teleport to Players"));
		p.getInventory().setItem(7, InfoBook.getBookItem());

		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
		if (hide) {
			for (Player p2 : getPlayers(false)) {
				p2.hidePlayer(p);
			}
		}

		p.setAllowFlight(true);
	}

	public static final int					innocentSlayLimit	= 3;

	public static HashMap<String, Integer>	innoSlay			= new HashMap<>();

	public static void onDeath(Player p, PlayerDeathEvent event) {
		p.getWorld().playSound(p.getLocation(), Sound.GHAST_SCREAM, 2, 1);

		Role r = Role.getRole(p);

		if (r != null) {
			r.players.remove(p.getName());

			if (p.getKiller() != null && Role.getRole(p.getKiller()) != null) {
				Role kr = Role.getRole(p.getKiller());
				MCShockwave.send(kr.color, p, "Person who killed you was a %s!", kr.name());

				if (kr != r) {
					if (kr == Role.Detective && r == Role.Traitor) {
						ShopManager.addCredits(p.getKiller().getName(), 2);
						MCShockwave.send(p.getKiller(), "+%s credits for killing %s", 2, p.getName());
					}
					if (kr == Role.Traitor && r != Role.Traitor) {
						ShopManager.addCredits(p.getKiller().getName(), 1);
						MCShockwave.send(p.getKiller(), "+%s credit for killing %s", 1, p.getName());
					}
				}

				if (kr != Role.Traitor && r != Role.Traitor) {
					int tb = 0;
					if (innoSlay.containsKey(p.getKiller().getName())) {
						tb = innoSlay.get(p.getKiller().getName());
						innoSlay.remove(p.getKiller().getName());
					}
					tb++;
					if (tb >= innocentSlayLimit) {
						p.getKiller().damage(p.getMaxHealth() * 10);
						p.getKiller().sendMessage(
								"§cYou were slain for having " + innocentSlayLimit + " innocent kills");
					} else {
						innoSlay.put(p.getKiller().getName(), tb);
						p.getKiller().sendMessage(
								"§aYou killed an innocent! If you kill " + (innocentSlayLimit - tb)
										+ " more, you will be slain!");
					}
				}
			}

			Material wepMat = null;
			String wepName = null;
			if (p.getKiller() != null) {
				ItemStack wep = p.getKiller().getItemInHand();
				if (wep != null && wep.getType() != Material.AIR) {
					wepMat = wep.getType();
					wepName = ItemMetaUtils.hasCustomName(wep) ? ChatColor.stripColor(ItemMetaUtils.getItemName(wep))
							: WordUtils.capitalizeFully(wepMat.name().replace('_', ' '));
				} else {
					wepMat = Material.STICK;
					wepName = "fist";
				}
			}
			new Corpse(p.getLocation(), p.getName(), DamageCauseInfo.getInfoFor(p.getLastDamageCause().getCause()),
					wepName, wepMat);
		}

		if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.PRIMED_TNT) {
			p.getPassenger().remove();
		}

		if (state == GameState.GAME) {
			if (Role.Traitor.getPlayers().size() == 0) {
				MCShockwave.broadcast(Role.Innocent.color, "%s win!", "Innocents");
				stop("Innocents");
			} else if (Role.Detective.getPlayers().size() == 0 && Role.Innocent.getPlayers().size() == 0) {
				MCShockwave.broadcast(Role.Traitor.color, "%s win!", "Traitors");
				stop("Traitors");
			}
		}

		updateScoreboards();
		updatePlayerLists();
	}

	public static void lobbyKit(Player p) {
		PlayerUtils.clearInv(p);
		p.getInventory().setItem(8, InfoBook.getBookItem());
	}

	public static void updatePlayerLists() {
		for (Player p : getPlayers(true)) {
			updatePlayerList(p);
		}
	}

	public static void updatePlayerList(Player p) {
		ArrayList<String> lo = new ArrayList<>();

		boolean showMIA = specs.contains(p.getName()) || Role.getRole(p) == Role.Traitor;

		ArrayList<String> alive = new ArrayList<>();
		ArrayList<String> mia = new ArrayList<>();
		ArrayList<String> comb = new ArrayList<>();
		for (Player a : getPlayers(false)) {
			String al = (Role.getRole(a) == Role.Detective ? "§9" : Role.getRole(a) == Role.Traitor
					&& Role.getRole(p) == Role.Traitor ? "§c" : "")
					+ a.getName();
			alive.add(al);
			comb.add(al);
		}
		for (String s : specs) {
			Corpse c = CorpseManager.getCorpseFromName(s);
			if (c != null && !c.identified) {
				mia.add(s);
				comb.add(s);
			}
		}

		Collections.sort(comb);

		lo.add("§a§nAlive");
		if (showMIA) {
			for (String s : alive) {
				lo.add(s);
			}
		} else {
			for (String s : comb) {
				lo.add(s);
			}
		}
		if (showMIA) {
			lo.add("§6§nMissing in Action");
			for (String s : mia) {
				lo.add(Role.getPastRole(s).color + s);
			}
		}
		lo.add("§4§nConfirmed Dead");
		for (String s : specs) {
			Corpse c = CorpseManager.getCorpseFromName(s);
			if (Role.getPastRole(s) != null && c != null && c.identified) {
				lo.add(Role.getPastRole(s).color + s);
			}
		}
		lo.add("§d§nSpectators");
		for (String s : specs) {
			if (Role.getPastRole(s) == null && Bukkit.getPlayer(s) != null) {
				lo.add(s);
			}
		}

		ItemStack set = new ItemStack(Material.PAPER);
		ItemMetaUtils.setItemName(set, "§6§lPlayer List");
		String[] lore = new String[lo.size()];
		for (int i = 0; i < lore.length; i++) {
			lore[i] = "§7" + lo.get(i);
		}
		ItemMetaUtils.setLore(set, lore);

		p.getInventory().setItem(9, set);
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
